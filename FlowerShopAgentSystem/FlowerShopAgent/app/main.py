"""
锦色花店 智能导购Agent - FastAPI 服务

核心功能:
- /chat             对话导购（推荐商品、构建订单）
- /order/{id}       获取当前订单
- /order/{id}/update  修改订单（增删商品、调整数量）
- /order/{id}/confirm  确认订单（提交到Spring Boot后端）
- /session/{id}/reset  重置会话
- /health           健康检查

架构:
  Frontend → FastAPI(本服务) → LangGraph Agent → MCP Server → MySQL
                                    ↓
                              Spring Boot（订单提交）
"""
import os
# 禁用系统代理，避免代理URL含中文导致ascii编码错误
os.environ["NO_PROXY"] = "*"
os.environ.pop("HTTP_PROXY", None)
os.environ.pop("HTTPS_PROXY", None)
os.environ.pop("http_proxy", None)
os.environ.pop("https_proxy", None)
# 阻止langchain注入自定义httpx transport，让我们的trust_env=False生效
os.environ["LANGCHAIN_OPENAI_TCP_KEEPALIVE"] = "0"

import json
import logging
import uuid
import yaml
from pathlib import Path
from contextlib import asynccontextmanager
from typing import Optional

from fastapi import FastAPI, HTTPException, UploadFile, File, Form
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from langchain_core.messages import HumanMessage, AIMessage, ToolMessage

from app.config import settings
from app.model_manager import model_manager
from app.graph.shopping_guide import create_shopping_guide_app
from app.mcp_client import init_mcp, shutdown_mcp, get_mcp_client
from app.models.schemas import (
    ChatRequest,
    ChatResponse,
    OrderItem,
    OrderUpdateRequest,
    OrderConfirmRequest,
    RecommendedOrder,
)

# 日志
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(name)s] %(levelname)s: %(message)s")
logger = logging.getLogger(__name__)

# 全局Agent和会话存储
_agent_app = None
from app.memory.session_store import session_manager


def _session_order(session_id: str) -> Optional[RecommendedOrder]:
    """获取会话中的当前订单"""
    s = session_manager.get(session_id)
    if s and s.get("order"):
        return s["order"]
    return None


# ═══════════════════════════════════════════
#  应用生命周期
# ═══════════════════════════════════════════

@asynccontextmanager
async def lifespan(app: FastAPI):
    global _agent_app

    # 连接MCP Server（所有数据和工具由MCP Server管理）
    mcp_ok = await init_mcp()
    if mcp_ok:
        logger.info("MCP Server连接成功，Agent将使用数据库数据")
    else:
        logger.warning("MCP Server不可用，Agent将使用本地知识库降级方案")

    # 编译LangGraph工作流
    _agent_app = create_shopping_guide_app()
    logger.info("LangGraph Agent工作流编译完成")

    yield

    # 关闭前保存内存中的会话到磁盘
    session_manager.save_all()
    logger.info(f"[lifespan] 已保存会话到磁盘")

    # 清理
    await shutdown_mcp()
    session_manager.clear()
    logger.info("Agent服务已关闭")


# ═══════════════════════════════════════════
#  FastAPI 应用
# ═══════════════════════════════════════════

app = FastAPI(
    title="锦色花店 - 智能导购Agent",
    description="基于LangGraph + MCP的鲜花导购智能助手，支持商品推荐、订单构建、活动查询",
    version="2.0.0",
    lifespan=lifespan,
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ═══════════════════════════════════════════
#  API 端点
# ═══════════════════════════════════════════


@app.post("/chat/stream")
async def chat_stream(request: ChatRequest):
    """
    流式对话接口（SSE）

    使用 Server-Sent Events 逐字推送 AI 回复，前端实现打字机效果。
    """
    import asyncio

    session_id = request.session_id or str(uuid.uuid4())[:8]
    user_id = request.user_id

    # 按需加载会话
    state = session_manager.get_or_create(session_id, user_id=user_id)

    # 运行 Agent（所有用户输入统一走 LangGraph + MCP）
    state["user_input"] = request.message
    state["messages"] = list(state.get("messages", []))
    state["messages"].append(HumanMessage(content=request.message))

    # 信息提取已由主图 node_extract_info 节点完成，无需在此手动处理
    logger.info(f"[chat] 状态信息: scenario={state.get('scenario_name') or state.get('identified_scenario')}, "
                f"scenario_id={state.get('scenario_id')}, recipient={state.get('recipient')}, "
                f"budget_min={state.get('budget_min')}, budget_max={state.get('budget_max')}, "
                f"category={state.get('preferred_category')}, round={state.get('_clarify_round', 0)}")

    async def gen():
        reply = "抱歉，处理您的请求时出现了问题，请稍后再试。"
        order = None
        btns = []

        try:
            result = await _agent_app.ainvoke(state)
            # 确保 session_id 和 user_id 不被 LangGraph 丢失
            result["session_id"] = session_id
            if user_id and not result.get("user_id"):
                result["user_id"] = user_id
            session_manager.save(session_id, result)

            # 提取 AI 回复
            msgs = result.get("messages", [])
            for m in reversed(msgs):
                if isinstance(m, AIMessage) and m.content and m.content.strip():
                    reply = m.content.strip()
                    break

            order = result.get("order")
            btns = _build_action_buttons(order) if order else []
        except Exception as e:
            logger.error(f"Agent流式异常: {e}", exc_info=True)
            reply = reply or "抱歉，处理您的请求时出现了问题，请稍后再试～"

        # 兜底：确保有回复内容
        if not reply or not reply.strip():
            reply = "抱歉，我暂时无法处理您的请求，请稍后再试～"

        # 逐字推送
        for ch in reply:
            yield f"data: {json.dumps({'type': 'token', 'content': ch})}\n\n"
            await asyncio.sleep(0.012)

        # 结束事件
        order_data = order.model_dump() if order and hasattr(order, 'model_dump') else None
        yield f"data: {json.dumps({'type': 'done', 'order': order_data, 'action_buttons': btns})}\n\n"

    return StreamingResponse(gen(), media_type="text/event-stream")


@app.get("/order/{session_id}", response_model=dict)
async def get_order(session_id: str):
    """获取当前会话的推荐订单"""
    order = _session_order(session_id)
    if not order:
        return {"has_order": False, "order": None}
    return {
        "has_order": True,
        "order": order.model_dump(),
    }


@app.post("/order/{session_id}/update", response_model=dict)
async def update_order(session_id: str, request: OrderUpdateRequest):
    """
    修改订单：增加/删除商品、调整数量、替换商品

    action 可选值:
    - add: 添加商品 (需要 product_id, quantity)
    - remove: 删除商品 (需要 product_id)
    - update_quantity: 修改数量 (需要 product_id, quantity)
    - replace: 替换商品 (需要 product_id, new_product_id)
    - clear: 清空订单
    """
    state = session_manager.get(session_id)
    if not state:
        raise HTTPException(status_code=404, detail="会话不存在")
    order = state.get("order")

    if request.action == "clear":
        state["order"] = None
        return {"success": True, "order": None}

    if not order:
        raise HTTPException(status_code=400, detail="当前没有订单")

    action_map = {"add": "add", "remove": "remove", "update_quantity": "update_quantity", "replace": "replace"}
    mapped = action_map.get(request.action, request.action)

    updated_order = _apply_order_action(state, {
        "action": mapped,
        "product_id": request.product_id,
        "quantity": request.quantity,
        "new_product_id": request.new_product_id,
    })

    session_manager.save(session_id, state)
    return {"success": True, "order": updated_order.model_dump() if updated_order else None}


@app.post("/order/{session_id}/confirm", response_model=dict)
async def confirm_order(session_id: str, request: OrderConfirmRequest):
    """
    确认订单，返回 {flower_id, quantity} 列表。
    实际提交和价格获取由前端直接调用 Spring Boot 后端完成。
    调用后清空 Agent 会话中的订单缓存。
    """
    order = _session_order(session_id)
    if not order or not order.items:
        raise HTTPException(status_code=400, detail="当前没有可提交的订单")

    # 构建简洁订单数据（仅 ID + 数量），前端根据 ID 从 Spring Boot 获取详情
    items_data = []
    for item in order.items:
        items_data.append({
            "flower_id": item.flower_id,
            "quantity": item.quantity,
        })

    order_payload = {
        "items": items_data,
        "scenario": order.scenario,
        "summary": order.summary,
        "tips": order.tips,
        "user_id": request.user_id or 1,
        "address_book_id": request.address_book_id or 1,
        "pay_method": request.pay_method or 1,
        "remark": request.remark or f"Agent推荐方案: {order.scenario}",
        "delivery_status": request.delivery_status or 1,
        "estimated_delivery_time": request.estimated_delivery_time or "",
    }

    # 清空 Agent 会话中的订单缓存
    state = session_manager.get(session_id)
    if state:
        state["order"] = None
        session_manager.save(session_id, state)

    logger.info(f"订单数据已组装: session={session_id}, items={len(items_data)}")

    return {
        "success": True,
        "message": "订单数据已就绪，请前端提交到 Spring Boot 后端",
        "order_payload": order_payload,
    }


@app.post("/session/{session_id}/reset")
async def reset_session(session_id: str):
    """重置会话（会话数据 + 持久化文件）"""
    session_manager.reset(session_id)
    return {"message": "会话已重置", "session_id": session_id}


@app.get("/session/{session_id}/history")
async def get_session_history(session_id: str):
    """获取会话历史消息，用于前端恢复聊天记录"""
    state = session_manager.get(session_id)
    if not state:
        return {"session_id": session_id, "messages": [], "has_history": False}
    messages_list = state.get("messages", [])

    history = []
    for msg in messages_list:
        if isinstance(msg, HumanMessage):
            history.append({"role": "user", "content": msg.content if isinstance(msg.content, str) else str(msg.content)})
        elif isinstance(msg, ToolMessage):
            content = msg.content if isinstance(msg.content, str) else str(msg.content)
            if content.strip():
                history.append({"role": "tool", "content": f"[工具] {content}"})
        elif isinstance(msg, AIMessage):
            content = msg.content if isinstance(msg.content, str) else str(msg.content)
            if content.strip():  # 跳过空内容的AI消息（如带tool_calls的AIMessage）
                history.append({"role": "bot", "content": content})

    # 获取当前订单状态
    order = state.get("order")
    order_data = order.model_dump() if order and hasattr(order, "model_dump") else None

    return {
        "session_id": session_id,
        "messages": history,
        "has_history": len(history) > 0,
        "current_step": state.get("current_step", "analyze"),
        "order": order_data,
    }


@app.get("/health")
async def health_check():
    """健康检查"""
    mcp_available = get_mcp_client().is_available
    return {
        "status": "ok",
        "version": "2.0.0",
        "mcp_connected": mcp_available,
        "active_sessions": len(list(session_manager.keys())),
        "llm_configured": bool(settings.LLM_BASE_URL),
        "llm_model": model_manager.llm_model,
        "embedding_model": model_manager.embedding_model,
    }


# ═══════════════════════════════════════════
#  模型热切换端点
# ═══════════════════════════════════════════

@app.get("/config")
async def get_config():
    """
    获取当前模型配置 + Ollama 上实际安装的模型列表
    模型列表通过 Ollama /api/tags 实时获取，不再依赖 config.yaml 白名单
    """
    models = await model_manager.list_ollama_models()
    llm_models = []
    embed_models = []
    for m in models:
        name = m.get("name", "")
        if any(kw in name.lower() for kw in ("embed", "bge", "e5", "all-minilm", "nomic")):
            embed_models.append(m)
        else:
            llm_models.append(m)

    return {
        **model_manager.get_full_config(),
        "available_llm_models": [m["name"] for m in llm_models],
        "available_embed_models": [m["name"] for m in embed_models],
        "all_models": models,
    }


@app.post("/config/switch-llm")
async def switch_llm(request: dict):
    """
    热切换大语言模型
    Body: {"model": "qwen3:14b", "base_url": "http://...", "temperature": 0.5, "max_tokens": 2048}
    """
    model = request.get("model")
    if not model:
        raise HTTPException(400, "缺少 model 参数")
    model_manager.switch_llm(
        model=model,
        base_url=request.get("base_url"),
        api_key=request.get("api_key"),
        temperature=request.get("temperature"),
        max_tokens=request.get("max_tokens"),
    )
    return {
        "success": True,
        "message": f"LLM 模型已切换为: {model}",
        "config": model_manager.get_llm_config(),
    }


@app.post("/config/switch-embedding")
async def switch_embedding(request: dict):
    """
    热切换嵌入模型
    Body: {"model": "nomic-embed-text", "base_url": "http://...", "top_k": 5}
    """
    model = request.get("model")
    if not model:
        raise HTTPException(400, "缺少 model 参数")
    model_manager.switch_embedding(
        model=model,
        base_url=request.get("base_url"),
        api_key=request.get("api_key"),
        top_k=request.get("top_k"),
    )
    return {
        "success": True,
        "message": f"Embedding 模型已切换为: {model}",
        "config": model_manager.get_embedding_config(),
    }


# ═══════════════════════════════════════════
#  测试连接端点
# ═══════════════════════════════════════════

@app.post("/config/test-connection")
async def test_connection(request: dict = None):
    """
    测试 LLM 和嵌入模型连接
    优先使用请求中传入的模型参数，否则使用 model_manager 运行时值
    Body: {"llm_model": "qwen3:14b", "llm_base_url": "...", "embedding_model": "..."}
    """
    import httpx

    request = request or {}
    results = {}

    # 使用请求参数，否则回退到 model_manager 运行时值
    llm_model = request.get("llm_model") or model_manager.llm_model
    llm_base_url = request.get("llm_base_url") or model_manager.llm_base_url
    embedding_model = request.get("embedding_model") or model_manager.embedding_model

    # 1. 测试 LLM 连接
    try:
        is_ollama = "11434" in llm_base_url or "ollama" in llm_base_url.lower()

        if is_ollama:
            ollama_base = llm_base_url.rstrip("/")
            if ollama_base.endswith("/v1"):
                ollama_base = ollama_base[:-3]
            test_resp = httpx.post(f"{ollama_base}/api/chat", json={
                "model": llm_model,
                "messages": [{"role": "user", "content": "Reply with only 'ok'."}],
                "stream": False,
            }, timeout=15)
            if test_resp.status_code == 404:
                test_resp = httpx.post(f"{ollama_base}/api/generate", json={
                    "model": llm_model,
                    "prompt": "Reply with only 'ok'.",
                    "stream": False,
                }, timeout=15)
            if test_resp.status_code == 200:
                results["llm"] = {"success": True, "message": f"LLM连接成功: {llm_model}"}
            else:
                results["llm"] = {"success": False, "message": f"LLM连接失败: HTTP {test_resp.status_code}"}
        else:
            test_url = f"{llm_base_url.rstrip('/')}/chat/completions"
            test_resp = httpx.post(test_url, json={
                "model": llm_model,
                "messages": [{"role": "user", "content": "Reply with only 'ok'."}],
                "temperature": 0,
                "max_tokens": 10,
            }, headers={"Authorization": f"Bearer {model_manager.llm_api_key}"} if model_manager.llm_api_key else {},
               timeout=15)
            if test_resp.status_code == 200:
                results["llm"] = {"success": True, "message": f"LLM连接成功: {llm_model}"}
            else:
                results["llm"] = {"success": False, "message": f"LLM连接失败: HTTP {test_resp.status_code}"}
    except Exception as e:
        results["llm"] = {"success": False, "message": f"LLM连接异常: {e}"}

    # 2. 测试嵌入模型连接（直连 Ollama，不走 MCP）
    try:
        embedding_base_url = request.get("embedding_base_url") or model_manager.embedding_base_url
        is_ollama = "11434" in embedding_base_url or "ollama" in embedding_base_url.lower()

        if is_ollama:
            ollama_base = embedding_base_url.rstrip("/")
            if ollama_base.endswith("/v1"):
                ollama_base = ollama_base[:-3]
            test_resp = httpx.post(f"{ollama_base}/api/embed", json={
                "model": embedding_model,
                "input": "test",
            }, timeout=15)
            if test_resp.status_code == 200:
                results["embedding"] = {"success": True, "message": f"嵌入模型连接正常: {embedding_model}"}
            else:
                results["embedding"] = {"success": False, "message": f"嵌入模型连接失败: HTTP {test_resp.status_code}"}
        else:
            # 非 Ollama：用 OpenAI 兼容的 embeddings 端点
            test_url = f"{embedding_base_url.rstrip('/')}/embeddings"
            test_resp = httpx.post(test_url, json={
                "model": embedding_model,
                "input": "test",
            }, headers={"Authorization": f"Bearer {model_manager.embedding_api_key}"} if model_manager.embedding_api_key else {},
               timeout=15)
            if test_resp.status_code == 200:
                results["embedding"] = {"success": True, "message": f"嵌入模型连接正常: {embedding_model}"}
            else:
                results["embedding"] = {"success": False, "message": f"嵌入模型连接失败: HTTP {test_resp.status_code}"}
    except Exception as e:
        results["embedding"] = {"success": False, "message": f"嵌入模型连接异常: {e}"}

    return {
        "success": results.get("llm", {}).get("success", False) and results.get("embedding", {}).get("success", False),
        "llm": results.get("llm", {}),
        "embedding": results.get("embedding", {}),
    }


# ═══════════════════════════════════════════
#  知识库管理端点（代理到 MCP Server）
# ═══════════════════════════════════════════

@app.get("/knowledge/documents")
async def list_documents():
    """列出知识库文档"""
    try:
        client = get_mcp_client()
        if not client.is_available:
            raise HTTPException(502, "MCP Server 未连接")
        result = await client.call_tool("tool_rag_categories", {})
        import json as _json
        categories = _json.loads(result) if isinstance(result, str) else result
        # get_knowledge_categories 返回 [{"category": ..., "documents": [{"filename": ..., "size": ...}]}]
        docs = []
        if isinstance(categories, list):
            for cat_info in categories:
                cat = cat_info.get("category", "")
                for doc in cat_info.get("documents", []):
                    docs.append({"category": cat, "filename": doc["filename"], "size": doc.get("size", 0)})
        elif isinstance(categories, dict):
            for cat, info in categories.items():
                if isinstance(info, dict) and "files" in info:
                    for f in info["files"]:
                        docs.append({"category": cat, "filename": f, "size": info.get("sizes", {}).get(f, 0)})
                else:
                    docs.append({"category": cat, "filename": str(info), "size": 0})
        return {"documents": docs, "total": len(docs)}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"查询知识库失败: {e}")


@app.post("/knowledge/upload")
async def upload_document(file: UploadFile = File(...), category: str = Form("general")):
    """上传文档到知识库"""
    try:
        client = get_mcp_client()
        if not client.is_available:
            raise HTTPException(502, "MCP Server 未连接")
        content = await file.read()
        text = content.decode("utf-8")
        result = await client.call_tool("tool_knowledge_upload", {
            "category": category,
            "filename": file.filename or "upload.txt",
            "content": text,
        })
        import json as _json
        return _json.loads(result) if isinstance(result, str) else result
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"上传失败: {e}")


@app.delete("/knowledge/documents/{category}/{filename}")
async def delete_document(category: str, filename: str):
    """删除知识库文档"""
    try:
        client = get_mcp_client()
        if not client.is_available:
            raise HTTPException(502, "MCP Server 未连接")
        result = await client.call_tool("tool_knowledge_delete", {
            "category": category,
            "filename": filename,
        })
        import json as _json
        return _json.loads(result) if isinstance(result, str) else result
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(500, f"删除失败: {e}")


# ═══════════════════════════════════════════
#  订单操作辅助函数
# ═══════════════════════════════════════════

def _apply_order_action(state: dict, action: dict) -> Optional[RecommendedOrder]:
    """执行订单修改操作（仅用于 REST /order/{id}/update 端点）"""
    order = state.get("order")
    act = action.get("action", "")

    if act == "clear":
        state["order"] = None
        return None

    if not order:
        return None

    items = list(order.items)

    if act == "remove":
        pid = action.get("product_id") or action.get("flower_id")
        items = [i for i in items if i.flower_id != pid]

    elif act == "update_quantity":
        pid = action.get("product_id") or action.get("flower_id")
        qty = max(1, action.get("quantity", 1))
        for item in items:
            if item.flower_id == pid:
                item.quantity = qty
                break

    order.items = items
    state["order"] = order
    return order


def _build_action_buttons(order: Optional[RecommendedOrder]) -> list[dict]:
    """为当前订单构建操作按钮"""
    if not order or not order.items:
        return []
    buttons = [
        {"text": "📋 查看/修改订单", "action": "view_order", "type": "primary"},
        {"text": "🔄 重新推荐", "action": "clear_order", "type": "default"},
    ]
    return buttons


# ═══════════════════════════════════════════
#  启动入口
# ═══════════════════════════════════════════

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=False,
    )

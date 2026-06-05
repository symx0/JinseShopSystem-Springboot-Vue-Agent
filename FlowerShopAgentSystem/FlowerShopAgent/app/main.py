import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from langchain_core.messages import HumanMessage

from app.config import settings
from app.graph.shopping_guide import create_shopping_guide_app
from app.knowledge import product_kb, scenario_kb
from app.models.schemas import ChatRequest, ChatResponse

logger = logging.getLogger(__name__)

# 会话存储（生产环境应使用Redis等持久化存储）
_sessions: dict[str, dict] = {}

# 全局Agent应用
_agent_app = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """应用生命周期管理"""
    global _agent_app

    # 初始化知识库
    product_kb.init_product_kb()
    scenario_kb.init_scenario_kb()
    logger.info("知识库初始化完成")

    # 编译Agent工作流
    _agent_app = create_shopping_guide_app()
    logger.info("Agent工作流初始化完成")

    yield

    _sessions.clear()


app = FastAPI(
    title="锦色花店 - 智能导购Agent",
    description="基于LangGraph的鲜花导购智能助手，根据用户场景需求推荐商品",
    version="1.0.0",
    lifespan=lifespan,
)


@app.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """与导购Agent对话"""
    session_id = request.session_id

    # 获取或创建会话状态
    if session_id not in _sessions:
        _sessions[session_id] = {
            "messages": [],
            "user_input": "",
            "identified_scenario": None,
            "scenario_confidence": 0.0,
            "extracted_keywords": [],
            "budget_range": None,
            "preferred_category": None,
            "recipient": None,
            "special_requirements": None,
            "need_more_info": False,
            "follow_up_question": None,
            "plan": None,
            "current_step": "analyze",
        }

    state = _sessions[session_id]

    # 更新用户输入
    state["user_input"] = request.message
    state["messages"] = list(state.get("messages", []))
    state["messages"].append(HumanMessage(content=request.message))

    # 运行Agent工作流
    result = _agent_app.invoke(state)

    # 更新会话状态
    _sessions[session_id] = result

    # 提取回复
    reply = ""
    for msg in reversed(result.get("messages", [])):
        if hasattr(msg, "content") and msg.__class__.__name__ == "AIMessage":
            reply = msg.content
            break

    if not reply:
        reply = "抱歉，我暂时无法处理您的请求，请稍后再试。"

    # 构建响应
    plan = result.get("plan")
    response = ChatResponse(
        reply=reply,
        plan=plan,
        need_more_info=result.get("need_more_info", False),
        session_id=session_id,
    )

    return response


@app.post("/session/{session_id}/reset")
async def reset_session(session_id: str):
    """重置会话"""
    if session_id in _sessions:
        del _sessions[session_id]
    return {"message": "会话已重置", "session_id": session_id}


@app.get("/health")
async def health_check():
    """健康检查"""
    return {
        "status": "ok",
        "products_count": len(product_kb.get_all_products()),
        "scenarios_count": len(scenario_kb.get_all_scenarios()),
        "active_sessions": len(_sessions),
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.HOST,
        port=settings.PORT,
        reload=True,
    )

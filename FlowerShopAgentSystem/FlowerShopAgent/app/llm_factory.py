"""LLM工厂 - 直接调用Ollama API，支持运行时模型热切换"""
import json
import logging
import httpx
import uuid
from typing import Optional
from app.config import settings
from app.model_manager import model_manager

logger = logging.getLogger(__name__)


def _get_base_url() -> str:
    """获取 Ollama 基础 URL（去掉 /v1 后缀）"""
    base = settings.LLM_BASE_URL.rstrip("/")
    if base.endswith("/v1"):
        base = base[:-3]
    return base


# ═══════════════════════════════════════════
#  统一 LLM 调用（支持工具调用）
# ═══════════════════════════════════════════

async def acall_llm_with_tools(
    messages: list[dict],
    tools: list[dict] = None,
    temperature: float = 0.7,
) -> dict:
    """
    异步调用 Ollama API（支持工具调用）。

    参数:
        messages: 对话消息列表，格式为 [{"role": "system/user/assistant/tool", "content": "..."}]
                  assistant 消息可包含 "tool_calls" 字段
                  tool 消息需包含 "tool_call_id" 字段
        tools: Ollama 格式的工具定义列表（可选）
        temperature: 生成温度

    返回:
        dict: {
            "content": str,          # 文本回复（可能为空）
            "tool_calls": list|None, # 工具调用列表，格式为 [{"id": "...", "function": {"name": "...", "arguments": {...}}}]
        }
    """
    base = _get_base_url()

    current_model = model_manager.llm_model
    payload = {
        "model": current_model,
        "messages": messages,
        "stream": False,
        "options": {"temperature": temperature},
    }

    if tools:
        payload["tools"] = tools

    async with httpx.AsyncClient(trust_env=False, timeout=180) as client:
        resp = await client.post(f"{base}/api/chat", json=payload)
        resp.raise_for_status()
        data = resp.json()

    msg = data.get("message", {})
    content = msg.get("content", "") or ""
    raw_tool_calls = msg.get("tool_calls")

    # 规范化 tool_calls 格式
    tool_calls = None
    if raw_tool_calls:
        tool_calls = []
        for tc in raw_tool_calls:
            func = tc.get("function", {})
            # 确保有 id
            tc_id = tc.get("id") or f"call_{uuid.uuid4().hex[:8]}"
            # 确保 arguments 是 dict
            args = func.get("arguments", {})
            if isinstance(args, str):
                try:
                    args = json.loads(args)
                except (json.JSONDecodeError, TypeError):
                    args = {}
            if not isinstance(args, dict):
                args = {}
            tool_calls.append({
                "id": tc_id,
                "function": {
                    "name": func.get("name", ""),
                    "arguments": args,
                },
            })

    logger.info(f"[acall_llm_with_tools] content长度={len(content)}, tool_calls={len(tool_calls) if tool_calls else 0}")

    # 同时生成 LangChain 格式的 tool_calls（用于 AIMessage.tool_calls）
    tool_calls_langchain = None
    if tool_calls:
        tool_calls_langchain = [
            {
                "name": tc["function"]["name"],
                "args": tc["function"]["arguments"],
                "id": tc["id"],
            }
            for tc in tool_calls
        ]

    return {
        "content": content,
        "tool_calls": tool_calls,
        "tool_calls_langchain": tool_calls_langchain,
    }


# ═══════════════════════════════════════════
#  嵌入模型 (Embedding) - Ollama
# ═══════════════════════════════════════════

def get_embedding(text: str) -> Optional[list[float]]:
    """
    获取文本的嵌入向量（通过 Ollama API）。
    当 EMBEDDING_MODE=tfidf 时返回 None（由 rag_tools 的 TF-IDF 处理）。
    """
    embed_mode = getattr(settings, "EMBEDDING_MODE", "tfidf")
    if embed_mode == "tfidf":
        return None

    base = model_manager.embedding_base_url.rstrip("/")
    if base.endswith("/v1"):
        base = base[:-3]
    embed_model = model_manager.embedding_model

    if not base:
        logger.warning("[embedding] 未配置嵌入模型 URL")
        return None

    url = f"{base}/api/embeddings"
    payload = {"model": embed_model, "prompt": text}

    try:
        with httpx.Client(trust_env=False, timeout=30) as client:
            resp = client.post(url, json=payload)
            resp.raise_for_status()
            data = resp.json()
        if data.get("embedding"):
            return data["embedding"]
        return None
    except Exception as e:
        logger.warning(f"[embedding] 调用失败: {e}")
        return None


def batch_get_embeddings(texts: list[str]) -> Optional[list[list[float]]]:
    """批量获取嵌入向量"""
    embed_mode = getattr(settings, "EMBEDDING_MODE", "tfidf")
    if embed_mode == "tfidf":
        return None

    results = []
    for text in texts:
        vec = get_embedding(text)
        if vec:
            results.append(vec)
        else:
            return None
    return results


# ═══════════════════════════════════════════
#  RAG 重排序 — BGE Reranker
# ═══════════════════════════════════════════

def _get_bge_reranker():
    """懒加载 BGE Reranker 模型"""
    global _bge_reranker
    if _bge_reranker is None:
        try:
            from FlagEmbedding import FlagReranker
            _bge_reranker = FlagReranker("BAAI/bge-reranker-base", use_fp16=True)
            logger.info("[rerank] BGE Reranker 模型加载成功: bge-reranker-base")
        except ImportError:
            logger.warning("[rerank] FlagEmbedding 未安装，将使用 LLM 降级重排序")
        except Exception as e:
            logger.warning(f"[rerank] BGE Reranker 加载失败: {e}，将使用 LLM 降级重排序")
    return _bge_reranker


async def rerank_rag_docs(query: str, documents: list[dict], top_k: int = None) -> list[dict]:
    """
    RAG 检索结果重排序。优先使用 BGE Reranker，不可用时降级为 LLM 重排序。

    参数:
        query: 用户查询文本
        documents: RAG检索返回的文档列表
        top_k: 保留的最大文档数，默认从 model_manager.rag_top_k 读取

    返回:
        重排序后的文档列表
    """
    if top_k is None:
        top_k = model_manager.rag_top_k
    if not documents:
        return []
    if len(documents) <= 1:
        return documents

    # 尝试 BGE Reranker
    reranker = _get_bge_reranker()
    if reranker is not None:
        try:
            pairs = []
            for doc in documents:
                text = doc.get("text", "") or doc.get("content", "")
                pairs.append([query, text])

            scores = reranker.compute_score(pairs, normalize=True)

            scored = list(zip(scores, documents))
            scored.sort(key=lambda x: x[0], reverse=True)
            reranked = [doc for s, doc in scored[:top_k] if s > 0.3]

            if reranked:
                logger.info(
                    f"[rerank] BGE重排序: {len(documents)}条 → {len(reranked)}条 "
                    f"(过滤{len(documents) - len(reranked)}条低相关, "
                    f"最高分={scored[0][0]:.3f}, 最低分={scored[-1][0]:.3f})"
                )
                return reranked
            else:
                logger.info(f"[rerank] BGE重排序: 全部低于阈值，返回原始前{top_k}条")
                return documents[:top_k]
        except Exception as e:
            logger.warning(f"[rerank] BGE重排序异常: {e}，降级为LLM")

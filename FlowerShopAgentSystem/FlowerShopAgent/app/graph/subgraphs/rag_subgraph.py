"""
子图: RAG 检索 + 知识回复 (RAG Subgraph)

功能:
  1. 通过 MCP RAG 工具检索知识库
  2. 格式化知识回复

用于: chitchat（闲聊）和 knowledge（花卉知识）路径

内部节点:
  rag_search → format_reply → END
"""
import json
import logging
from typing import TypedDict, Optional

from langchain_core.messages import AIMessage, BaseMessage
from langgraph.graph import StateGraph, END

from app.config import settings
from app.model_manager import model_manager

logger = logging.getLogger(__name__)


class RAGState(TypedDict, total=False):
    """RAG 子图内部状态"""
    user_input: str
    user_type: str
    extracted_keywords: list[str]
    # 输出
    rag_documents: list[dict]
    messages: list[BaseMessage]



async def node_rag_search(state: RAGState) -> dict:
    """从 MCP RAG 工具检索知识"""
    user_input = state.get("user_input", "")
    keywords = state.get("extracted_keywords", [])
    user_type = state.get("user_type", "knowledge")

    query = user_input
    if keywords and user_type == "knowledge":
        # 知识类：用关键词增强查询
        query = user_input + " " + " ".join(keywords)

    rag_docs = []

    # 尝试 MCP RAG 检索
    try:
        from app.mcp_client import get_mcp_client
        client = get_mcp_client()
        if client.is_available:
            result = await client.call_tool("tool_rag_search", {"query": query, "top_k": model_manager.rag_initial_top_k})
            if result:
                try:
                    parsed = json.loads(result) if isinstance(result, str) else result
                    if isinstance(parsed, list):
                        rag_docs = parsed
                except (json.JSONDecodeError, TypeError):
                    pass
    except Exception as e:
        logger.warning(f"MCP RAG 检索失败: {e}")

    # BGE 重排序
    if rag_docs and len(rag_docs) > 1:
        try:
            from app.llm_factory import rerank_rag_docs
            rag_docs = await rerank_rag_docs(user_input, rag_docs, top_k=model_manager.rag_top_k)
        except Exception as e:
            logger.warning(f"RAG重排序失败: {e}")

    return {"rag_documents": rag_docs}


def node_format_reply(state: RAGState) -> dict:
    """格式化知识回复"""
    user_type = state.get("user_type", "knowledge")
    user_input = state.get("user_input", "")
    rag_docs = state.get("rag_documents", [])

    if user_type == "chitchat":
        reply = _build_chitchat_reply(user_input)
    elif rag_docs:
        # RAG 有结果 → 使用检索到的知识
        reply = _build_knowledge_from_docs(user_input, rag_docs)
    else:
        # RAG 无结果 → 尝试用 LLM 生成特定回复
        llm_reply = _try_llm_knowledge_reply(user_input)
        if llm_reply:
            reply = llm_reply
        else:
            # RAG 和 LLM 都不可用 → 明确告知用户
            reply = (
                f"抱歉，关于「{user_input[:20]}」我暂时没有找到相关的知识储备 🌸\n\n"
                "您可以尝试：\n"
                "• 换个关键词描述，比如具体的花种名称\n"
                "• 问我关于鲜花养护、花语含义、场景选花等问题\n"
                "• 直接告诉我您的买花需求，我帮您推荐！"
            )

    return {"messages": [AIMessage(content=reply)]}


def _build_chitchat_reply(user_input: str) -> str:
    """闲聊回复"""
    text = user_input.lower()
    if any(w in text for w in ["你好", "嗨", "hi", "hello", "在吗"]):
        return (
            "您好！我是锦色花店的智能导购小锦 🌸\n\n"
            "我可以帮您：\n"
            "🌹 **根据场景推荐鲜花** — 告诉我您的需求，比如送给谁、什么场合\n"
            "💰 **查看促销活动** — 帮您搭配更优惠的方案\n"
            "💡 **花卉养护知识** — 花语含义、养护技巧、场景选花指南\n\n"
            "请问今天想选什么花呢？"
        )
    if any(w in text for w in ["谢谢", "感谢", "3q"]):
        return "不客气！有什么需要随时找我哦~ 🌸"
    if any(w in text for w in ["再见", "拜拜", "bye"]):
        return "再见！祝您生活愉快，有需要随时找我~ 🌺"
    return "您好！有什么可以帮您的吗？选花、问知识、看活动都可以问我~ 🌸"


def _try_llm_knowledge_reply(user_input: str) -> str:
    """尝试用 LLM 生成知识回复（当 RAG MCP 不可用时）"""
    if not bool(settings.LLM_BASE_URL):
        return ""
    try:
        from app.llm_factory import call_llm
        system_prompt = (
            "你是锦色花店的资深花卉顾问，精通各种鲜花的养护知识、花语含义、品种特性和场景搭配。"
            "请直接以纯文本回复，不要使用任何函数调用或工具。"
            "回复要专业、准确、有针对性，针对用户问的具体花卉品种给出相应的知识。"
        )
        content = call_llm(system_prompt=system_prompt, user_message=user_input, temperature=0.7)
        if content:
            return content + "\n\n💡 如果您有具体的买花需求，也可以告诉我哦~"
        return ""
    except Exception as e:
        logger.warning(f"LLM知识回复失败: {e}")
        return ""


def _build_knowledge_from_docs(user_input: str, rag_docs: list) -> str:
    """从 RAG 检索结果构建知识回复"""
    parts = ["📖 为您找到以下相关知识：\n"]
    for i, doc in enumerate(rag_docs[:3], 1):
        doc_text = doc.get("text", "") or doc.get("content", "") or ""
        doc_category = doc.get("category", "") or doc.get("source", "")
        if doc_text:
            header = f"**{doc_category}**" if doc_category else ""
            parts.append(f"{i}. {header}\n{doc_text[:500]}\n")
    parts.append("\n💡 如果您有具体的买花需求，也可以告诉我哦~")
    return "\n".join(parts)


def _build_knowledge_reply(user_input: str, rag_docs: list) -> str:
    """知识回复降级模板（RAG 和 LLM 都不可用时使用）"""
    text = user_input.lower()

    # 降级：模板回复
    if any(w in text for w in ["养", "浇", "保鲜", "换水", "修剪", "保存", "浇水"]):
        return (
            "💐 **鲜花养护要点**\n\n"
            "• 45度斜剪花茎底部1-2厘米，增加吸水面积\n"
            "• 每天换水保持清洁，去除水下叶片防止腐烂\n"
            "• 避免阳光直射和空调出风口\n"
            "• 远离成熟水果（苹果、香蕉释放乙烯加速凋谢）\n"
            "• 加少量白糖或鲜花保鲜剂延长花期\n"
            "• 玫瑰花外层保护瓣摘除即可，不是不新鲜\n\n"
            "📖 更多养护知识可以问我具体的花种~"
        )

    if any(w in text for w in ["花语", "含义", "寓意", "代表", "象征"]):
        return (
            "🌸 **常见花语含义**\n\n"
            "🌹 红玫瑰 — 热烈的爱情、深爱\n"
            "💗 粉玫瑰 — 初恋、温柔、感动\n"
            "🤍 白玫瑰 — 纯洁、尊敬\n"
            "💛 向日葵 — 阳光、忠诚、敬慕\n"
            "💐 康乃馨 — 母爱、感恩、温馨\n"
            "🌿 满天星 — 真爱、关怀、纯洁的爱\n"
            "🔥 红掌 — 红红火火、吉祥如意\n"
            "🦋 蝴蝶兰 — 幸福向你飞来\n\n"
            "想知道具体某种花的花语可以继续问我~"
        )

    if any(w in text for w in ["送", "场合", "推荐", "适合", "选什么"]):
        return (
            "🎁 **场景选花指南**\n\n"
            "💑 表白/求婚 → 玫瑰花束（11朵=一心一意）\n"
            "👩 母亲节 → 康乃馨\n"
            "🎓 毕业季 → 向日葵+满天星（前程似锦）\n"
            "🏠 乔迁 → 红掌/盆栽（红红火火）\n"
            "🏢 开业 → 红掌/发财树（生意兴隆）\n"
            "🏥 探病 → 小盆栽/多肉（好养护）\n"
            "🧧 春节 → 水仙花（吉祥如意）\n\n"
            "您具体是什么场景呢？我帮您细选~"
        )

    return (
        "您可以问我这些问题哦~ 🌸\n\n"
        "💐 花怎么养护、怎么保鲜？\n"
        "🌸 各种花的花语含义？\n"
        "🎁 不同场合该送什么花？\n"
        "🌿 盆栽怎么养护？\n"
        "💰 有什么促销活动？\n\n"
        "也可以直接告诉我您的买花需求，我帮您搭配推荐！"
    )


def create_rag_subgraph() -> StateGraph:
    """创建 RAG 检索子图"""
    graph = StateGraph(RAGState)

    graph.add_node("rag_search", node_rag_search)
    graph.add_node("format_reply", node_format_reply)

    graph.set_entry_point("rag_search")
    graph.add_edge("rag_search", "format_reply")
    graph.add_edge("format_reply", END)

    return graph.compile()
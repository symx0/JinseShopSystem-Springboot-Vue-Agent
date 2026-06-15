"""
子图: 意图分类 (Classify Intent Subgraph)

功能:
  1. 结合会话总结(thinking_chain)、历史对话和当前用户输入，分类意图为闲聊/知识/购物
  2. 提取关键词
  3. 作为整个工作流的入口

全部由 LLM 判断，无任何硬编码关键词。
"""
import json
import logging
import re
from typing import TypedDict

from langgraph.graph import StateGraph, END
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage, ToolMessage

from app.config import settings

logger = logging.getLogger(__name__)


class ClassifyIntentState(TypedDict, total=False):
    """意图分类子图内部状态"""
    user_input: str
    thinking_chain: str  # 会话总结（过往问答摘要）
    messages: list[BaseMessage]  # 历史对话消息
    # 输出
    user_type: str  # chitchat | knowledge | shopping
    intent_confidence: float
    extracted_keywords: list[str]


CLASSIFY_PROMPT = """你是一个花店导购系统的意图分类器。请直接以纯文本JSON格式回复。

你需要结合【会话总结】、【近期对话】和【当前用户消息】来综合判断用户的意图。

意图类型说明:
- chitchat: 打招呼/闲聊/感谢/再见/与买花无关的日常对话
- knowledge: 询问花卉养护知识/品种介绍/行业知识/花语含义/场景选花指南/咨询商品/查看活动（不涉及具体购买）
- shopping: 有购买需求/想要推荐/下单/修改订单/补充购物需求信息/确认或拒绝订单

注意：
- 用户可能在购物流程中简短回复（如商品名、数量、预算），这些都属于 shopping
- 如果用户之前在购物流程中，当前消息是对之前推荐的跟进（如提到推荐过的花名），应分类为 shopping
- 如果用户之前在介绍流程中，当前消息是对之前推荐的跟进（如提到推荐过的花名），应分类为 Knowledge
- 如果用户在购物流程中补充需求信息（如"预算100左右""送给妈妈"），仍为 shopping
- 如果用户修改已有信息（如"预算充裕""预算没问题""预算不是问题""预算可以放宽"），应分类为 shopping
- 如果用户对推荐做出确认或拒绝（如"ok""行""可以""好的""就这个""下单""算了""不要这个"），应分类为 shopping
- 如果用户要求恢复之前的订单（如"按照我原来的订单""给我之前的方案"），应分类为 shopping
- 只有当用户明确转向与买花完全无关的话题时，才分类为 chitchat
- 近期对话提供了上下文，帮助你理解用户当前消息的真实意图，避免误判

请以JSON格式输出（只输出JSON）：
{{
    "user_type": "chitchat" | "knowledge" | "shopping",
    "confidence": 0.0-1.0,
    "keywords": ["关键词1", "关键词2"]
}}"""


def _format_history(messages: list[BaseMessage], max_turns: int = 6) -> str:
    """将历史消息格式化为文本，取最近 max_turns 轮"""
    if not messages:
        return ""

    # 过滤掉 ToolMessage，只保留 Human 和 AI 的对话
    filtered = [m for m in messages if isinstance(m, (HumanMessage, AIMessage)) and not (
        isinstance(m, AIMessage) and m.tool_calls
    )]
    # 取最近 max_turns 条
    recent = filtered[-max_turns:]

    if not recent:
        return ""

    lines = []
    for m in recent:
        content = m.content if isinstance(m.content, str) else str(m.content)
        if not content.strip():
            continue
        role = "用户" if isinstance(m, HumanMessage) else "助手"
        lines.append(f"{role}: {content}")

    return "\n".join(lines)


def _llm_available() -> bool:
    return bool(settings.LLM_BASE_URL)


async def node_llm_classify(state: ClassifyIntentState) -> dict:
    user_input = state.get("user_input", "")
    thinking_chain = state.get("thinking_chain", "")
    messages = state.get("messages", [])

    if not _llm_available():
        logger.warning("[classify] LLM 不可用，默认返回 shopping")
        return {
            "user_type": "shopping",
            "intent_confidence": 0.3,
            "extracted_keywords": [],
        }

    try:
        from app.llm_factory import acall_llm_with_tools

        # 组装用户消息：包含会话总结 + 近期对话 + 当前输入
        parts = []
        if thinking_chain:
            parts.append(f"【会话总结】\n{thinking_chain}")

        history_text = _format_history(messages)
        if history_text:
            parts.append(f"【近期对话】\n{history_text}")

        parts.append(f"【当前用户消息】\n{user_input}")

        user_message = "\n\n".join(parts)

        result = await acall_llm_with_tools(
            messages=[
                {"role": "system", "content": CLASSIFY_PROMPT},
                {"role": "user", "content": user_message},
            ],
            temperature=0.2,
        )
        content = result["content"]

        if not content:
            logger.warning("[classify] LLM 返回空内容，默认 shopping")
            return {"user_type": "shopping", "intent_confidence": 0.3, "extracted_keywords": []}

        analysis = _parse_json(content)
        logger.info(f"[classify] LLM结果: type={analysis.get('user_type')}, conf={analysis.get('confidence')}")

        return {
            "user_type": analysis.get("user_type", "shopping"),
            "intent_confidence": analysis.get("confidence", 0.5),
            "extracted_keywords": analysis.get("keywords", []),
        }
    except Exception as e:
        logger.warning(f"[classify] LLM 分类失败: {e}，默认 shopping")
        return {"user_type": "shopping", "intent_confidence": 0.3, "extracted_keywords": []}


def _parse_json(text: str) -> dict:
    """安全解析JSON"""
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        pass
    match = re.search(r'```(?:json)?\s*([\s\S]*?)\s*```', text)
    if match:
        try:
            return json.loads(match.group(1))
        except json.JSONDecodeError:
            pass
    match = re.search(r'\{[\s\S]*\}', text)
    if match:
        try:
            return json.loads(match.group(0))
        except json.JSONDecodeError:
            pass
    return {}


# ═══════════════════════════════════════════
#  构建子图
# ═══════════════════════════════════════════

def create_classify_intent_subgraph() -> StateGraph:
    """创建意图分类子图（LLM only，无规则降级）"""
    graph = StateGraph(ClassifyIntentState)

    graph.add_node("llm_classify", node_llm_classify)

    graph.set_entry_point("llm_classify")
    graph.add_edge("llm_classify", END)

    return graph.compile()
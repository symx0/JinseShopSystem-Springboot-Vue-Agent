"""
主图 (Main Graph) — LangGraph 导购Agent编排层 (重构版v6)

架构（模型驱动工具调用）:
  START → classify → [路由]
    ├→ query_chitchat → hallucination_check → respond → summarize → END (闲聊：模型自主决定是否调用工具)
    ├→ query_knowledge → hallucination_check → respond → summarize → END (知识：模型自主调用RAG/商品工具)
    └→ extract_info → query_and_recommend → hallucination_check → respond → summarize → END (购物→推荐)

  核心变化：删除 ask_missing 节点和所有 fallback 模板，
  模型自行根据会话历史决定是否追问，不再由代码硬编码追问逻辑。

  thinking_chain: 会话总结(str)，每次交互后由 summarize 节点用 LLM 更新，
                  下次 classify 时结合会话总结+当前输入分析意图。
"""
import json
import logging
import re
from typing import Optional

from langgraph.graph import StateGraph, END
from langchain_core.messages import AIMessage, HumanMessage

from app.config import settings
from app.graph.state import AgentState
from app.graph.subgraphs import (
    create_classify_intent_subgraph,
    create_respond_subgraph,
)
from app.models.schemas import OrderItem, RecommendedOrder
from app.model_manager import model_manager

from app.llm_factory import acall_llm_with_tools
logger = logging.getLogger(__name__)


def _build_history_messages(state_messages: list, max_turns: int = 6) -> list[dict]:
    """将 AgentState.messages 中的历史对话转为 Ollama API 格式，取最近 max_turns 条"""
    if not state_messages:
        return []

    filtered = [
        m for m in state_messages
        if isinstance(m, (HumanMessage, AIMessage)) and not (isinstance(m, AIMessage) and m.tool_calls)
    ]
    recent = filtered[-max_turns:]

    result = []
    for m in recent:
        content = m.content if isinstance(m.content, str) else str(m.content)
        if not content.strip():
            continue
        role = "user" if isinstance(m, HumanMessage) else "assistant"
        result.append({"role": role, "content": content})

    return result


# ═══════════════════════════════════════════
#  主图节点
# ═══════════════════════════════════════════

async def node_classify_intent(state: AgentState) -> dict:
    """意图分类节点 — 结合会话总结、历史对话和当前输入分析意图"""
    classify_graph = create_classify_intent_subgraph()
    sub_state = {
        "user_input": state.get("user_input", ""),
        "thinking_chain": state.get("thinking_chain", ""),
        "messages": state.get("messages", []),
    }
    result = await classify_graph.ainvoke(sub_state)
    user_type = result.get("user_type", "shopping")
    logger.info(f"[main] 意图分类: type={user_type}, conf={result.get('intent_confidence')}")

    return {
        "user_type": user_type,
        "intent_confidence": result.get("intent_confidence", 0.5),
        "extracted_keywords": list(set(state.get("extracted_keywords", []) + result.get("extracted_keywords", []))),
        "current_step": "classify",
    }


async def node_extract_info(state: AgentState) -> dict:
    """
    信息提取节点（shopping 路径入口）
    用 LLM 从用户当前输入 + 对话历史中提取场景、对象、预算、偏好等信息。
    每次调用都会尝试提取，有新值就更新（用户可能修正之前的信息）。
    """
    user_input = state.get("user_input", "")
    if not user_input or not user_input.strip():
        return {"current_step": "extract_info"}

    try:

        # 组装已有信息
        known_parts = []
        if state.get("scenario_name") or state.get("identified_scenario"):
            known_parts.append(f"场景={state.get('scenario_name') or state.get('identified_scenario')}")
        if state.get("recipient"):
            known_parts.append(f"对象={state.get('recipient')}")
        if state.get("budget_min") is not None or state.get("budget_max") is not None:
            bmin = state.get("budget_min", 0)
            bmax = state.get("budget_max", "不限")
            known_parts.append(f"预算=¥{bmin}~¥{bmax}")
        if state.get("preferred_category"):
            known_parts.append(f"偏好={state.get('preferred_category')}")
        if state.get("special_requirements"):
            known_parts.append(f"特殊要求={state.get('special_requirements')}")
        known = "、".join(known_parts) if known_parts else "尚无已知信息"

        # 构建对话历史
        history = _build_history_messages(state.get("messages", []), max_turns=10)
        history_text = ""
        if history:
            history_lines = []
            for h in history:
                role_label = "用户" if h["role"] == "user" else "助手"
                history_lines.append(f"{role_label}: {h['content']}")
            history_text = "\n".join(history_lines)

        prompt = (
            f"请结合【已有信息】、【对话历史】和【当前用户消息】，输出完整的需求信息JSON。\n"
            f"规则：\n"
            f"- 必须结合对话历史理解用户的指代（如\"第三种\"\"就这个方案\"\"第一款\"\"第二个\"等），将指代内容解析为具体需求。特别要注意：当助手在上一轮列出了多个选项（如\"1. XXX 2. YYY\"），用户说\"第X款\"时，要找到对应选项的具体内容\n"
            f"- 如果当前消息提供了新信息，用新值覆盖已有值\n"
            f"- 如果当前消息没有提到某个字段，保留已有信息中的值，不要填null，如果新需求和旧需求重合，以新的消息为准，覆盖旧需求\n"
            f"- 只有确实没有任何来源的信息时才填null\n\n"
            f"已有信息：{known}\n\n"
        )
        if history_text:
            prompt += f"对话历史：\n{history_text}\n\n"
        prompt += (
            f"当前用户消息：{user_input}\n\n"
            f"请输出以下字段的完整值：\n"
            f"- scenario_name: 场景中文描述（2-4字，如\"母亲节\"\"生日\"\"表白\"\"自用装饰\"等）\n"
            f"- scenario_confidence: 场景置信度（0.0-1.0）\n"
            f"- scenario_id: 场景英文ID（用下划线，如\"mother_day\"\"birthday\"\"confession\"\"home_decor\"等）\n"
            f"- recipient: 送礼对象中文（如\"母亲\"\"恋人\"\"朋友\"\"客户\"\"自用\"等）\n"
            f"- budget_min: 最小预算（整数数字）。【严禁推断】只在用户明确说出数字（如\"100左右\"\"50到80\"\"不超过200\"）时才填写，不要根据商品价格、场景、对话内容推断。用户没有明确说预算就填null\n"
            f"- budget_max: 最大预算（整数数字）。同budget_min规则，用户没明确说就填null。如果用户说\"预算充裕\"\"预算没问题\"\"预算不是问题\"\"预算可以放宽\"等表示预算不再受限，填99999\n"
            f"- category: 偏好类别（\"花束\"\"盆栽\"\"多肉\"\"园林造景\"中的一个，用户没明确说就填null）\n"
            f"- special_requirements: 特殊要求（如颜色偏好\"粉色\"、花种偏好\"玫瑰\"、包装要求等，合并已有要求；必须解析对话历史中的指代，如用户选了\"第三种\"对应的具体内容；如果用户说\"按照我原来的订单\"\"给我之前的方案\"，从前面的对话中找到用户最初的需求；用户没明确提出就填null）\n\n"
            f'示例输出：{{"scenario_name":"母亲节","scenario_confidence":0.9,"scenario_id":"mother_day","recipient":"母亲","budget_min":70,"budget_max":130,"category":"花束","special_requirements":"红玫瑰配满天星"}}'
        )

        result = await acall_llm_with_tools(
            messages=[
                {"role": "system", "content": "你是信息提取助手，只输出JSON。必须结合对话历史理解用户指代。"},
                {"role": "user", "content": prompt},
            ],
            temperature=0.7,
        )
        content = result["content"]

        result = json.loads(content.strip().removeprefix("```json").removesuffix("```").strip())

        updates = {"current_step": "extract_info"}

        # 场景信息：LLM 返回完整值，非 null 就更新
        if result.get("scenario_name"):
            updates["scenario_name"] = result["scenario_name"]
            logger.info(f"[extract_info] 场景名: {result['scenario_name']}")

        if result.get("scenario_confidence"):
            updates["scenario_confidence"] = result["scenario_confidence"]

        if result.get("scenario_id"):
            updates["scenario_id"] = result["scenario_id"]
            updates["identified_scenario"] = result["scenario_id"]
            logger.info(f"[extract_info] 场景ID: {result['scenario_id']}")

        # 对象信息
        if result.get("recipient"):
            updates["recipient"] = result["recipient"]
            logger.info(f"[extract_info] 对象: {result['recipient']}")

        # 预算信息：LLM 返回完整值，非 null 就更新；null 清除旧值（降级兜底）
        bmin_raw = result.get("budget_min")
        if bmin_raw is not None:
            updates["budget_min"] = float(bmin_raw)
            logger.info(f"[extract_info] 预算最小值: {bmin_raw}")

        bmax_raw = result.get("budget_max")
        if bmax_raw is not None:
            updates["budget_max"] = float(bmax_raw)
            logger.info(f"[extract_info] 预算最大值: {bmax_raw}")
        elif "budget_max" in result:
            updates["budget_max"] = None
            logger.info("[extract_info] 预算上限已清除")

        # 偏好类别
        if result.get("category"):
            updates["preferred_category"] = result["category"]
            logger.info(f"[extract_info] 偏好: {result['category']}")

        # 特殊要求
        if result.get("special_requirements"):
            updates["special_requirements"] = result["special_requirements"]
            logger.info(f"[extract_info] 特殊要求: {result['special_requirements']}")

        return updates

    except Exception as e:
        logger.warning(f"[extract_info] LLM 信息提取失败: {e}")
        return {"current_step": "extract_info"}


async def node_query_knowledge(state: AgentState) -> dict:
    """
    知识查询节点（knowledge 路径）— 模型驱动工具调用
    模型自主决定调用哪些工具（RAG检索、商品搜索等），循环直到生成最终回复。
    工具定义从 MCP Server 动态获取，无需硬编码。
    """
    from app.mcp_client import get_mcp_client

    client = get_mcp_client()
    tools = client.get_ollama_tools() if client.is_available else []
    user_input = state.get("user_input", "")
    thinking_chain = state.get("thinking_chain", "")

    # 构建对话消息
    system_prompt = (
        "你是锦色花店的资深花卉顾问，精通各种鲜花的养护知识、花语含义、品种特性和场景搭配。"
        "请根据用户的问题，自主决定是否调用工具查询相关知识或商品信息来辅助回答。"
        "回答要专业、准确、有针对性。"
        "重要约束："
        "1. 你只能建议花店实际提供的服务，严禁编造不存在的能力（如'线上订购''查询附近花店''推荐其他平台'等），花店是一家实体花店，只提供店内选购和店内配送服务"
        "2. 如果工具查询结果为空，明确告知用户当前没有该商品，只从工具查询到的实际在售商品中推荐替代方案，不要编造不存在的信息"
        "3. 如果用户询问的商品花店没有，只需说明没有并推荐替代品，不要建议用户去其他平台购买"
        "4. 【严禁】绝对禁止在回复中输出任何ID信息。包括：活动ID、商品ID、分类ID、SKU编号、数据库主键等。"
        "   ❌ 错误: \"端午促销（活动ID：13）\""
        "   ✅ 正确: \"端午促销\""
        "   ❌ 错误: \"如需了解某个活动的具体商品，可以告诉我活动ID\""
        "   ✅ 正确: \"如需了解某个活动的具体商品，告诉我活动名称即可\""
        "   只展示商品名称、价格、描述、活动名称等用户关心的信息，永远不要让用户接触任何ID"
        "5. 涉及查询商品、活动、知识时，必须先调用工具获取真实数据，再基于数据回答，不要凭记忆编造"
    )
    messages = [{"role": "system", "content": system_prompt}]
    if thinking_chain:
        messages.append({"role": "system", "content": f"【会话总结】\n{thinking_chain}"})
    # 加入历史对话，避免模型重复自我介绍
    history = _build_history_messages(state.get("messages", []))
    messages.extend(history)
    messages.append({"role": "user", "content": user_input})

    new_messages = []
    rag_docs = []
    raw_products = None
    max_rounds = 2

    for _ in range(max_rounds):
        result = await acall_llm_with_tools(messages=messages, tools=tools or None, temperature=0.7)

        if result.get("tool_calls"):
            # 模型决定调用工具（仅用于内部 messages，不写入 state.messages）
            tc_list = result["tool_calls"]
            assistant_msg = {"role": "assistant", "content": result.get("content", ""), "tool_calls": tc_list}
            messages.append(assistant_msg)

            # 执行每个工具调用
            for tc in tc_list:
                func_name = tc["function"]["name"]
                func_args = tc["function"]["arguments"]
                tool_call_id = tc["id"]

                try:
                    tool_result = await client.call_tool(func_name, func_args)
                    # 收集特定工具的结果到 state
                    if func_name == "tool_rag_search":
                        try:
                            parsed = json.loads(tool_result) if isinstance(tool_result, str) else tool_result
                            if isinstance(parsed, list):
                                rag_docs = parsed
                        except (json.JSONDecodeError, TypeError):
                            pass
                    elif func_name == "tool_get_all_flowers_with_promos":
                        raw_products = tool_result
                except Exception as e:
                    logger.warning(f"[query_knowledge] 工具调用失败 {func_name}: {e}")
                    tool_result = f"工具调用失败: {e}"

                result_content = str(tool_result)

                tool_msg = {"role": "tool", "content": result_content, "tool_call_id": tool_call_id}
                messages.append(tool_msg)
        else:
            # 模型返回文本回复，结束循环
            content = result.get("content", "")
            if content.strip():
                new_messages.append(AIMessage(content=content))
            break

    # 保底：循环结束后若未生成文本回复（LLM陷入反复调工具的死循环），强制生成一次
    if not new_messages:
        logger.warning("[query_knowledge] 工具循环耗尽未生成回复，执行保底文本生成")
        messages.append({
            "role": "system",
            "content": "你已经完成了工具查询，现在请基于以上所有查询结果，直接回答用户的问题。不要再调用任何工具，直接输出纯文本回复。"
        })
        try:
            fallback = await acall_llm_with_tools(messages=messages, tools=None, temperature=0.7)
            content = fallback.get("content", "")
            if content and content.strip():
                new_messages.append(AIMessage(content=content.strip()))
            else:
                logger.warning("[query_knowledge] 保底文本生成为空")
        except Exception as e:
            logger.error(f"[query_knowledge] 保底文本生成失败: {e}")

    # BGE 重排序
    if rag_docs and len(rag_docs) > 1:
        try:
            from app.llm_factory import rerank_rag_docs
            rag_docs = await rerank_rag_docs(user_input, rag_docs, top_k=model_manager.rag_top_k)
        except Exception as e:
            logger.warning(f"[query_knowledge] RAG重排序失败: {e}")

    return {
        "rag_documents": rag_docs,
        "_raw_products": raw_products,
        "messages": new_messages,
        "current_step": "knowledge",
    }


async def node_query_chitchat(state: AgentState) -> dict:
    """
    闲聊节点（chitchat 路径）— 模型驱动工具调用
    模型自主决定是否调用工具（如RAG检索），循环直到生成最终回复。
    工具定义从 MCP Server 动态获取，无需硬编码。
    """
    from app.mcp_client import get_mcp_client

    client = get_mcp_client()
    tools = client.get_ollama_tools() if client.is_available else []
    user_input = state.get("user_input", "")
    thinking_chain = state.get("thinking_chain", "")

    system_prompt = (
        "你是锦色花店的智能导购助手小锦，热情专业，像一位懂花的朋友。"
        "如果用户的问题涉及花卉知识、商品、活动，必须先调用工具查询真实数据再回答，不要凭记忆编造。"
        "如果只是日常闲聊，直接自然友好地回复即可，适当引导用户了解鲜花相关服务。"
        "【严禁】绝对禁止在回复中输出任何ID信息（活动ID、商品ID、分类ID、SKU等）。"
        "永远不要让用户接触任何ID，只展示名称、价格、描述等用户关心的信息。"
    )
    messages = [{"role": "system", "content": system_prompt}]
    if thinking_chain:
        messages.append({"role": "system", "content": f"【会话总结】\n{thinking_chain}"})
    # 加入历史对话，避免模型重复自我介绍
    history = _build_history_messages(state.get("messages", []))
    messages.extend(history)
    messages.append({"role": "user", "content": user_input})

    new_messages = []
    max_rounds = 3

    for _ in range(max_rounds):
        result = await acall_llm_with_tools(messages=messages, tools=tools or None, temperature=0.7)

        if result.get("tool_calls"):
            # 模型决定调用工具（仅用于内部 messages，不写入 state.messages）
            tc_list = result["tool_calls"]
            assistant_msg = {"role": "assistant", "content": result.get("content", ""), "tool_calls": tc_list}
            messages.append(assistant_msg)

            for tc in tc_list:
                func_name = tc["function"]["name"]
                func_args = tc["function"]["arguments"]
                tool_call_id = tc["id"]

                try:
                    tool_result = await client.call_tool(func_name, func_args)
                except Exception as e:
                    logger.warning(f"[query_chitchat] 工具调用失败 {func_name}: {e}")
                    tool_result = f"工具调用失败: {e}"

                result_content = str(tool_result)
                if len(result_content) > 2000:
                    result_content = result_content[:2000] + "...(结果已截断)"

                tool_msg = {"role": "tool", "content": result_content, "tool_call_id": tool_call_id}
                messages.append(tool_msg)
        else:
            content = result.get("content", "")
            if content.strip():
                new_messages.append(AIMessage(content=content))
            break

    return {
        "messages": new_messages,
        "current_step": "chitchat",
    }

RECOMMEND_PROMPT = """
你是一个花店导购助手。请直接以纯文本JSON格式回复，不要使用任何函数调用或工具。

根据以下信息为用户推荐商品方案。

用户需求:
- 场景: {scenario}
- 预算: {budget}
- 送礼对象: {recipient}
- 偏好: {category}
- 要求: {requirements}

{conversation_context}

候选商品（来自数据库，含实时价格和活动）:
{candidates}

当前活动:
{activities}

RAG知识库参考:
{rag_info}

请从候选商品中选择3-5个组成推荐方案。优先选有促销活动的商品。
以JSON格式输出（只输出JSON，不要输出任何其他内容）：
{{
    "recommendations": [
        {{
            "product_id": 商品ID(int，必须来自候选商品列表中的ID),
            "quantity": 推荐数量(1-3)
        }}
    ],
    "scenario_name": "场景中文名称",
    "summary": "方案概述（1-2句话，包含总预算参考）",
    "tips": "实用建议（如养护提醒、搭配建议）"
}}

重要规则：
1. product_id 必须是候选商品列表中存在的ID，不要编造
2. 不要输出价格字段，价格由前端从后端实时获取
3. 只输出JSON，不要输出任何解释文字
4. 必须尊重用户在对话中明确表达的选择！如果用户在对话中指定了具体花材（如\"百合\"\"满天星\"），必须在候选商品列表中查找该花材。如果用户说\"第X款\"\"第一个\"\"要这个\"等指代，必须结合对话历史找到对应商品
5. 如果用户指定了具体花材但候选列表中没有该花材，返回空的recommendations列表，并在summary中说明\"花店暂无[花材名]\"
6. 如果用户没有指定具体花材（只是模糊需求如\"送妈妈\"），则从候选列表中选择最接近的进行推荐，至少推荐1个商品
7. 如果没有精确匹配但用户未指定具体花材，选择同类别、相近价位或适合类似场景的商品作为替代推荐"""


async def node_query_and_recommend(state: AgentState) -> dict:
    """
    商品查询+推荐节点（shopping 路径）
    1. 模型自主调用 MCP 工具获取商品/活动/RAG 数据
    2. 基于获取的数据，由 LLM 生成推荐方案
    """
    from app.config import settings
    from app.mcp_client import get_mcp_client

    if not settings.LLM_BASE_URL:
        return {"order": None, "_recommend_error": "LLM未配置", "current_step": "recommend"}

    client = get_mcp_client()
    tools = client.get_ollama_tools() if client.is_available else []
    user_input = state.get("user_input", "")
    thinking_chain = state.get("thinking_chain", "")

    # ── 阶段1：模型自主调用工具获取数据 ──
    need_parts = []
    if state.get("scenario_name") or state.get("identified_scenario"):
        need_parts.append(f"场景={state.get('scenario_name') or state.get('identified_scenario')}")
    if state.get("recipient"):
        need_parts.append(f"对象={state.get('recipient')}")
    if state.get("budget_min") is not None or state.get("budget_max") is not None:
        bmin = state.get("budget_min", 0)
        bmax = state.get("budget_max", "不限")
        need_parts.append(f"预算=¥{bmin}~¥{bmax}")
    if state.get("preferred_category"):
        need_parts.append(f"偏好={state.get('preferred_category')}")
    need_text = "、".join(need_parts) if need_parts else "暂无明确需求"

    system_prompt = (
        "你是锦色花店的数据查询助手。请根据用户的需求，自主调用工具查询商品和活动信息。\n"
        "规则：\n"
        "1. 调用工具获取商品和活动数据，不要自己编造\n"
        "2. 如果需要了解花卉知识，可以调用 RAG 检索工具\n"
        "3. 获取到足够数据后，回复\"数据查询完成\"即可，不要生成推荐文案\n"
        "4. 不要向用户直接推荐商品，推荐由后续流程处理"
    )
    messages = [{"role": "system", "content": system_prompt}]
    if thinking_chain:
        messages.append({"role": "system", "content": f"【会话总结】\n{thinking_chain}"})
    history = _build_history_messages(state.get("messages", []))
    messages.extend(history)
    messages.append({"role": "user", "content": f"用户需求：{need_text}\n用户消息：{user_input}"})

    raw_products = None
    raw_activities = None
    raw_knowledge = None
    max_rounds = 6

    logger.info(f"[query_and_recommend] 开始工具调用循环, tools数量={len(tools)}, MCP可用={client.is_available}")

    for _ in range(max_rounds):
        result = await acall_llm_with_tools(messages=messages, tools=tools or None, temperature=0.7)
        logger.info(f"[query_and_recommend] LLM返回: content长度={len(result.get('content',''))}, tool_calls={len(result.get('tool_calls',[])) if result.get('tool_calls') else 0}")

        if result.get("tool_calls"):
            tc_list = result["tool_calls"]
            assistant_msg = {"role": "assistant", "content": result.get("content", ""), "tool_calls": tc_list}
            messages.append(assistant_msg)

            for tc in tc_list:
                func_name = tc["function"]["name"]
                func_args = tc["function"]["arguments"]
                tool_call_id = tc["id"]

                try:
                    tool_result = await client.call_tool(func_name, func_args)
                    if func_name == "tool_get_all_flowers_with_promos":
                        raw_products = tool_result
                    elif func_name == "tool_get_ongoing_activities":
                        raw_activities = tool_result
                    elif func_name == "tool_rag_search":
                        raw_knowledge = tool_result
                    elif func_name == "tool_search_flowers":
                        if not raw_products:
                            raw_products = tool_result
                    elif func_name == "tool_search_by_scenario":
                        if not raw_products:
                            raw_products = tool_result
                except Exception as e:
                    logger.warning(f"[query_and_recommend] 工具调用失败 {func_name}: {e}")
                    tool_result = f"工具调用失败: {e}"

                result_content = str(tool_result)
                if len(result_content) > 4000:
                    result_content = result_content[:4000] + "...(结果已截断)"

                tool_msg = {"role": "tool", "content": result_content, "tool_call_id": tool_call_id}
                messages.append(tool_msg)
        else:
            break

    # 保底：如果模型没有获取完整商品数据，自动补充
    logger.info(f"[query_and_recommend] 工具循环结束, raw_products={'有' if raw_products else '无'}, raw_activities={'有' if raw_activities else '无'}, raw_knowledge={'有' if raw_knowledge else '无'}")
    if not raw_products and client.is_available:
        try:
            raw_products = await client.call_tool("tool_get_all_flowers_with_promos")
            logger.info("[query_and_recommend] 保底补充获取完整商品数据")
        except Exception as e:
            logger.warning(f"[query_and_recommend] 保底获取商品数据失败: {e}")
    if not raw_activities and client.is_available:
        try:
            raw_activities = await client.call_tool("tool_get_ongoing_activities")
        except Exception as e:
            logger.warning(f"[query_and_recommend] 保底获取活动数据失败: {e}")

    # ── 阶段2：基于数据生成推荐方案 ──
    try:
        all_products = json.loads(raw_products) if isinstance(raw_products, str) else raw_products
        activities = json.loads(raw_activities) if isinstance(raw_activities, str) else raw_activities
        rag_results = json.loads(raw_knowledge) if isinstance(raw_knowledge, str) else raw_knowledge
    except (json.JSONDecodeError, TypeError):
        return {"order": None, "_recommend_error": "数据格式异常", "current_step": "recommend"}

    # BGE 重排序
    if isinstance(rag_results, list) and len(rag_results) > 1:
        try:
            from app.llm_factory import rerank_rag_docs
            need_text = (state.get("identified_scenario") or "") + " "
            need_text += " ".join(state.get("extracted_keywords", []))
            if not need_text.strip():
                need_text = state.get("user_input", "") or "鲜花推荐"
            rag_results = await rerank_rag_docs(need_text.strip(), rag_results, top_k=model_manager.rag_top_k)
        except Exception as e:
            logger.warning(f"[query_and_recommend] RAG重排序失败: {e}")

    if not all_products:
        logger.warning(f"[query_and_recommend] 无商品数据, raw_products={raw_products[:200] if raw_products else 'None'}")
        return {"order": None, "_recommend_error": "暂无商品数据", "current_step": "recommend"}

    try:
        # 构建精简候选文本
        candidate_lines = []
        for p in all_products[:40]:
            promo = "🔥活动" if p.get("is_promo") else ""
            orig = f"原价{p['original_price']}" if p.get("original_price") else ""
            candidate_lines.append(
                f"ID:{p['id']} | {p['name']} | {p.get('category_name','')} | "
                f"¥{p['price']} {orig} | {p.get('description','')[:40]} {promo}"
            )

        rag_summary = ""
        if isinstance(rag_results, list) and rag_results:
            rag_summary = "\n".join(
                f"[{r.get('category','')}] {r.get('text','')[:200]}" for r in rag_results[:3]
            )

        budget_min = state.get("budget_min")
        budget_max = state.get("budget_max")
        budget_str = "不限"
        if budget_min is not None or budget_max is not None:
            bmin = budget_min if budget_min is not None else 0
            bmax = budget_max if budget_max is not None else "不限"
            budget_str = f"¥{bmin}~¥{bmax}"

        # 构建对话上下文（让推荐LLM理解用户在对话中的具体选择）
        conversation_context = ""
        history = _build_history_messages(state.get("messages", []), max_turns=10)
        if history:
            history_lines = []
            for h in history:
                role_label = "用户" if h["role"] == "user" else "助手"
                history_lines.append(f"{role_label}: {h['content']}")
            conversation_context = f"对话历史（用户可能在对话中指定了具体花材或搭配，必须尊重）：\n" + "\n".join(history_lines)
        elif thinking_chain:
            conversation_context = f"会话总结：{thinking_chain}"

        rec_prompt = RECOMMEND_PROMPT.format(
            scenario=state.get("identified_scenario", "通用推荐"),
            budget=budget_str,
            recipient=state.get("recipient", "未明确"),
            category=state.get("preferred_category", "不限"),
            requirements=state.get("special_requirements", "无"),
            conversation_context=conversation_context,
            candidates="\n".join(candidate_lines),
            activities=json.dumps(activities, ensure_ascii=False)[:500] if activities else "无",
            rag_info=rag_summary or "无",
        )

        result = await acall_llm_with_tools(
            messages=[
                {"role": "system", "content": rec_prompt},
                {"role": "user", "content": "请根据以上信息进行推荐"},
            ],
            temperature=0.7,
        )
        content = result["content"]
        logger.info(f"[query_and_recommend] 推荐LLM返回: content长度={len(content)}, 前200字={content[:200]}")
        rec_data = _parse_json(content)
        logger.info(f"[query_and_recommend] 推荐解析结果: recommendations数量={len(rec_data.get('recommendations', []))}")

        # 构建 RecommendedOrder
        items = []
        for rec in rec_data.get("recommendations", []):
            pid = rec.get("product_id")
            if pid is None:
                continue
            if not any(p["id"] == pid for p in all_products):
                logger.warning(f"[query_and_recommend] 推荐了不存在的商品ID: {pid}，已跳过")
                continue
            qty = max(1, min(rec.get("quantity", 1), 999))
            items.append(OrderItem(flower_id=pid, quantity=qty))

        order = RecommendedOrder(
            items=items,
            scenario=rec_data.get("scenario_name", state.get("identified_scenario", "个性化推荐")),
            summary=rec_data.get("summary", ""),
            tips=rec_data.get("tips", ""),
        )

        # 如果推荐结果为空（LLM无法从候选中匹配用户需求），标记原因
        if not items:
            summary = rec_data.get("summary", "")
            error_msg = summary if summary else "花店暂无您需要的商品"
            logger.info(f"[query_and_recommend] 推荐结果为空，原因: {error_msg}")
            return {
                "order": None,
                "_raw_products": raw_products,
                "_recommend_error": error_msg,
                "current_step": "recommend",
            }

        return {"order": order, "_raw_products": raw_products, "current_step": "recommend"}

    except Exception as e:
        import traceback
        logger.warning(f"[query_and_recommend] 推荐生成失败: {e}\n{traceback.format_exc()}")
        return {"order": None, "_recommend_error": "智能推荐暂时不可用", "current_step": "recommend"}


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


async def node_check_hallucination(state: AgentState) -> dict:
    """
    LLM 统一处理：幻觉检测 + 敏感信息清理 + 商品校验（所有路径必经）

    一次 LLM 调用完成全部质检工作：
    - 购物路径：校验商品是否真实存在/可购买、检测文本幻觉、清理敏感信息
    - 闲聊/知识路径：检测文本幻觉、清理敏感信息
    """
    order = state.get("order")
    raw_products = state.get("_raw_products")
    user_type = state.get("user_type", "shopping")
    messages = list(state.get("messages", []))

    # 找到最后一条 AIMessage
    target_idx = None
    for i in range(len(messages) - 1, -1, -1):
        m = messages[i]
        if hasattr(m, "type") and m.type == "ai" and m.content and m.content.strip():
            target_idx = i
            break

    if target_idx is None:
        return {"_order_valid": True, "_hallucination_flags": [], "current_step": "verify", "messages": messages}

    original_text = messages[target_idx].content

    # 构建 LLM 输入：AI 回复 + 商品数据（购物路径）+ 订单项（购物路径）
    llm_input = _build_hallucination_check_input(original_text, raw_products, order, user_type)

    # 调用 LLM 统一质检，返回 JSON
    try:
        llm_result = await _call_hallucination_llm(llm_input)
    except Exception as e:
        logger.warning(f"[hallucination] LLM 调用失败: {e}，跳过质检")
        return {"_order_valid": True, "_hallucination_flags": [], "current_step": "verify", "messages": messages}

    # 解析 LLM 返回
    cleaned_text = llm_result.get("cleaned_text", original_text)
    hallucinations = llm_result.get("hallucinations", [])
    order_validation = llm_result.get("order_validation")

    # 更新消息文本
    if cleaned_text and cleaned_text != original_text:
        messages[target_idx].content = cleaned_text
        logger.info(f"[hallucination] LLM 质检完成，发现 {len(hallucinations)} 处幻觉")

    # 购物路径：根据 LLM 校验结果更新 order
    if user_type == "shopping" and order and order.items and order_validation:
        valid_item_ids = set(order_validation.get("valid_item_ids", []))
        removed_items = order_validation.get("removed_items", [])
        all_valid = order_validation.get("all_valid", len(removed_items) == 0)

        if not valid_item_ids:
            # 全部商品无效
            reason = order_validation.get("unavailable_reason", "您需求的商品目前无法购买")
            logger.info(f"[hallucination] 所有商品被过滤: {reason}")
            return {
                "_hallucination_flags": removed_items,
                "_order_valid": False,
                "_recommend_failed_rounds": 0,
                "_recommend_error": reason,
                "order": None,
                "current_step": "verify",
                "messages": messages,
            }

        # 过滤 order.items，保留 LLM 判定有效的商品
        order.items = [it for it in order.items if it.flower_id in valid_item_ids]

        # 更新数量（LLM 可能调整了库存不足商品的数量）
        for item_update in order_validation.get("items", []):
            fid = item_update.get("flower_id")
            qty = item_update.get("quantity")
            if fid and qty is not None:
                for order_item in order.items:
                    if order_item.flower_id == fid:
                        order_item.quantity = qty
                        break

        return {
            "_hallucination_flags": removed_items,
            "_order_valid": all_valid,
            "_recommend_failed_rounds": 0,
            "order": order,
            "current_step": "verify",
            "messages": messages,
        }

    return {
        "_order_valid": True,
        "_hallucination_flags": [],
        "current_step": "verify",
        "messages": messages,
    }


def _build_hallucination_check_input(text: str, raw_products, order, user_type: str) -> str:
    """构建 LLM 质检输入：AI 回复 + 真实数据，供 LLM 对比校验"""
    parts = [f"【需要质检的AI回复】\n{text}"]

    if user_type == "shopping" and raw_products:
        try:
            products = json.loads(raw_products) if isinstance(raw_products, str) else raw_products
            if products:
                product_data = json.dumps(products, ensure_ascii=False, indent=2)
                parts.append(f"\n【真实商品数据库（权威数据源，用于校验AI回复中的商品是否真实存在）】\n{product_data}")
        except (json.JSONDecodeError, TypeError):
            pass

    if user_type == "shopping" and order and order.items:
        order_items = [
            {"flower_id": it.flower_id, "name": it.name or f"商品#{it.flower_id}", "quantity": it.quantity}
            for it in order.items
        ]
        if order_items:
            parts.append(f"\n【当前订单中的商品（需校验每个商品是否在真实数据库中且可购买）】\n{json.dumps(order_items, ensure_ascii=False, indent=2)}")

    return "\n".join(parts)


async def _call_hallucination_llm(input_text: str) -> dict:
    """调用 LLM 进行统一质检，返回 JSON"""
    try:
        from app.llm_factory import acall_llm_with_tools

        system_prompt = """你是一个花店AI质检助手。你的任务是对AI助手的回复进行全面质检：

## 一、商品校验（仅购物路径，若输入中有商品数据则必须执行）
逐一检查订单中每个商品：
- 商品是否在真实数据库中？（flower_id 必须匹配）
- 商品状态是否为可购买？（status 为 1 表示在售）
- 库存是否充足？（stock > 0，且不小于订单数量）
将不可购买的商品放入 removed_items，可购买的放入 valid_item_ids。

## 二、文本幻觉检测
检查AI回复中是否编造了以下内容：
- 数据库中不存在的商品名称、花材品种、颜色、花语
- 与数据不符的价格、促销信息
- 凭空捏造的养护知识、包装规格、配送规则
- 对用户需求的错误理解或过度承诺
如发现幻觉，在 hallucinations 数组中记录类型和详情。

## 三、敏感信息清理
从回复文本中移除不应展示给顾客的内部数据：
- 数据库ID编号（活动ID、商品ID、分类ID等）
- 图片链接（http/https URL）
- 库存、销量、限购数、活动内容等运营字段
- 原始JSON数据结构
- 引导用户使用ID的句子
保留正常文本格式（列表、加粗**等）和用户应看到的信息（名称、价格、描述）。

## 输出格式（严格遵守JSON）
```json
{
  "cleaned_text": "清理后的回复文本",
  "hallucinations": [
    {"type": "商品不存在|虚假属性|错误价格|编造知识|其他", "detail": "具体描述"}
  ],
  "order_validation": {
    "all_valid": true,
    "valid_item_ids": [1, 3],
    "removed_items": [
      {"flower_id": 5, "name": "商品名", "reason": "库存不足"}
    ],
    "items": [
      {"flower_id": 1, "valid": true, "quantity": 1}
    ],
    "unavailable_reason": "如果全部不可购买，填写原因；否则为null"
  }
}
```
注意：order_validation 仅在购物路径且有订单数据时返回，否则设为 null。"""

        messages = [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": input_text},
        ]
        result = await acall_llm_with_tools(messages=messages, tools=None, temperature=0.0)
        content = result.get("content", "").strip()

        # 解析 JSON
        return _parse_llm_json_response(content)
    except Exception as e:
        logger.warning(f"[hallucination] LLM 调用异常: {e}")
        raise


def _parse_llm_json_response(content: str) -> dict:
    """解析 LLM 返回的 JSON，支持 markdown 代码块包裹"""
    import re

    # 尝试提取 ```json ... ``` 代码块
    json_match = re.search(r'```(?:json)?\s*([\s\S]*?)\s*```', content)
    if json_match:
        json_str = json_match.group(1)
    else:
        json_str = content

    try:
        return json.loads(json_str)
    except json.JSONDecodeError:
        # 尝试修复常见问题：截断到最后一个 } 再次解析
        last_brace = json_str.rfind('}')
        if last_brace > 0:
            try:
                return json.loads(json_str[:last_brace + 1])
            except json.JSONDecodeError:
                pass
        logger.warning(f"[hallucination] JSON 解析失败，降级返回原始文本")
        return {"cleaned_text": content, "hallucinations": []}


async def node_respond(state: AgentState) -> dict:
    """
    统一回复生成节点。
    对于 chitchat/knowledge 路径：如果模型在工具调用循环中已生成回复，直接使用。
    对于 shopping 路径：仍需调用 respond 子图处理订单展示。
    """
    user_type = state.get("user_type", "shopping")

    # chitchat/knowledge 路径：如果模型已生成回复，跳过
    if user_type in ("chitchat", "knowledge"):
        existing_messages = state.get("messages", [])
        has_llm_reply = False
        for m in reversed(existing_messages):
            if isinstance(m, AIMessage) and m.content and m.content.strip() and not m.tool_calls:
                has_llm_reply = True
                break

        if has_llm_reply:
            logger.info("[main] 检测到工具调用循环中已生成回复，跳过respond子图")
            return {"current_step": "respond"}

    # shopping 路径或无模型回复时：调用 respond 子图
    respond_graph = create_respond_subgraph()

    # 意图映射：chitchat/knowledge 直接用 user_type，shopping 走 recommend
    if user_type in ("chitchat", "knowledge"):
        intent = user_type
    else:
        intent = "recommend"

    sub_state = {
        "intent": intent,
        "user_input": state.get("user_input", ""),
        "order": state.get("order"),
        "identified_scenario": state.get("identified_scenario"),
        "scenario_name": state.get("scenario_name"),
        "recipient": state.get("recipient"),
        "budget_min": state.get("budget_min"),
        "budget_max": state.get("budget_max"),
        "preferred_category": state.get("preferred_category"),
        "special_requirements": state.get("special_requirements"),
        "rag_documents": state.get("rag_documents", []),
        "_hallucination_flags": state.get("_hallucination_flags", []),
        "_order_valid": state.get("_order_valid", True),
        "_recommend_failed_rounds": state.get("_recommend_failed_rounds", 0),
        "_recommend_error": state.get("_recommend_error"),
        "_raw_products": state.get("_raw_products", "{}"),
        "messages": list(state.get("messages", [])),
    }
    result = await respond_graph.ainvoke(sub_state)
    messages = result.get("messages", [])
    logger.info(f"[main] 回复生成: intent={intent}, msgs={len(messages)}")

    return {
        "messages": messages,
        "current_step": "end",
    }


async def node_summarize(state: AgentState) -> dict:
    """
    会话总结节点 — 每次交互后用LLM总结历史对话，更新thinking_chain。
    thinking_chain 作为会话总结供下次意图分类时理解上下文。
    使用异步LLM调用避免阻塞事件循环。
    """
    messages = state.get("messages", [])
    prev_summary = state.get("thinking_chain", "")

    # 消息太少时不需要总结
    if len(messages) < 2:
        return {}

    if not _llm_available():
        return {}

    try:
        from app.llm_factory import acall_llm_with_tools

        # 构建对话文本（最近10轮）
        recent = messages[-10:]
        dialog_lines = []
        for m in recent:
            if m.type == "human":
                role = "用户"
            elif m.type == "tool":
                role = "工具"
            elif m.type == "system":
                role = "系统"
            else:
                role = "助手"
            content = m.content[:200] if len(m.content) > 200 else m.content
            if content.strip():
                dialog_lines.append(f"{role}: {content}")
        dialog_text = "\n".join(dialog_lines)

        if prev_summary:
            user_msg = (
                f"【之前的会话总结】\n{prev_summary}\n\n"
                f"【新增对话】\n{dialog_text}\n\n"
                f"请结合之前的总结和新增对话，生成一份更新后的会话总结。"
                f"总结应包含：用户的核心需求、已确认的信息（场景/对象/预算/偏好）、意图变化、未解决的问题。"
                f"控制在150字以内。"
            )
        else:
            user_msg = (
                f"【对话内容】\n{dialog_text}\n\n"
                f"请总结这段对话。总结应包含：用户的核心需求、已确认的信息（场景/对象/预算/偏好）、意图变化、未解决的问题。"
                f"控制在150字以内。"
            )

        result = await acall_llm_with_tools(
            messages=[
                {"role": "system", "content": "你是会话总结助手，简洁准确地总结对话要点。"},
                {"role": "user", "content": user_msg},
            ],
            temperature=0.3,
        )
        summary = result["content"]

        if summary and summary.strip():
            logger.info(f"[summarize] 会话总结已更新: {summary.strip()[:80]}...")
            return {"thinking_chain": summary.strip()}

    except Exception as e:
        logger.warning(f"[summarize] 会话总结失败: {e}")

    return {}


def _llm_available() -> bool:
    return bool(settings.LLM_BASE_URL)


# ═══════════════════════════════════════════
#  路由函数
# ═══════════════════════════════════════════

def _route_after_classify(state: AgentState) -> str:
    """
    意图路由:
    - chitchat → query_chitchat（LLM 自主回复，可能调用工具）
    - knowledge → query_knowledge（MCP 查询知识库/商品）
    - shopping → extract_info（信息提取）
    """
    user_type = state.get("user_type", "shopping")
    if user_type == "chitchat":
        return "chitchat"
    if user_type == "knowledge":
        return "knowledge"
    return "shopping"


def _check_needs_complete(state: AgentState) -> str:
    """
    extract_info 后的路由：直接进入推荐流程。
    模型自行根据会话历史决定是否追问，不再由代码判断。
    """
    logger.info("[route_after_extract] 直接进入商品查询与推荐")
    return "query_and_recommend"


# ═══════════════════════════════════════════
#  构建主图
# ═══════════════════════════════════════════

def build_main_graph() -> StateGraph:
    """
    构建主图（模型驱动工具调用版）

    流程:
      START → classify → [路由]
        ├→ query_chitchat → hallucination_check → respond → summarize → END (闲聊：模型自主决定是否调用工具)
        ├→ query_knowledge → hallucination_check → respond → summarize → END (知识：模型自主调用RAG/商品工具)
        └→ extract_info → query_and_recommend → hallucination_check → respond → summarize → END (购物→推荐)
    """
    graph = StateGraph(AgentState)

    # 注册节点
    graph.add_node("classify", node_classify_intent)
    graph.add_node("extract_info", node_extract_info)
    graph.add_node("query_chitchat", node_query_chitchat)
    graph.add_node("query_knowledge", node_query_knowledge)
    graph.add_node("query_and_recommend", node_query_and_recommend)
    graph.add_node("hallucination_check", node_check_hallucination)
    graph.add_node("respond", node_respond)
    graph.add_node("summarize", node_summarize)

    # 入口
    graph.set_entry_point("classify")

    # 意图路由
    graph.add_conditional_edges(
        "classify",
        _route_after_classify,
        {
            "chitchat": "query_chitchat",
            "knowledge": "query_knowledge",
            "shopping": "extract_info",
        },
    )

    # 闲聊路径: query_chitchat → hallucination_check → respond
    graph.add_edge("query_chitchat", "hallucination_check")

    # 知识路径: query_knowledge → hallucination_check → respond
    graph.add_edge("query_knowledge", "hallucination_check")

    # 购物路径: extract_info → query_and_recommend → hallucination_check → respond
    graph.add_edge("extract_info", "query_and_recommend")
    graph.add_edge("query_and_recommend", "hallucination_check")
    graph.add_edge("hallucination_check", "respond")

    # 所有路径经过 respond 后，统一进入 summarize 更新会话总结
    graph.add_edge("respond", "summarize")
    graph.add_edge("summarize", END)

    return graph


def create_shopping_guide_app():
    """编译并返回导购Agent应用（主图入口）"""
    graph = build_main_graph()
    app = graph.compile()
    logger.info("导购Agent主图编译完成")
    return app
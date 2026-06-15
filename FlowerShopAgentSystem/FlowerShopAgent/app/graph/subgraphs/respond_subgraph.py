"""
子图: 响应生成 (Respond Subgraph) — 重构版v3

功能:
  1. 根据意图类型和上下文生成对应回复
  2. 闲聊：LLM 直接生成自然回复
  3. 知识：结合 RAG 检索结果 + LLM 生成知识回答
  4. 购物：处理推荐订单、追问、幻觉检查等

内部节点:
  route_and_reply → END
"""
import json
import logging
from typing import TypedDict, Optional

from langchain_core.messages import AIMessage, HumanMessage
from langgraph.graph import StateGraph, END

from app.config import settings
from app.models.schemas import RecommendedOrder

logger = logging.getLogger(__name__)


class RespondState(TypedDict, total=False):
    """响应生成子图内部状态"""
    intent: str
    user_input: str
    order: Optional[RecommendedOrder]
    identified_scenario: Optional[str]
    scenario_name: Optional[str]
    recipient: Optional[str]
    budget_min: Optional[float]
    budget_max: Optional[float]
    preferred_category: Optional[str]
    special_requirements: Optional[str]
    # RAG 检索结果（knowledge 路径）
    rag_documents: list[dict]
    # 幻觉检查
    _hallucination_flags: list[dict]
    _order_valid: bool
    _recommend_failed_rounds: int
    _recommend_error: Optional[str]  # 推荐失败原因
    # MCP 原始数据（用于根据 flower_id 查找商品名称和价格生成回复文本）
    _raw_products: Optional[str]
    # 输出
    messages: list
    _reply_type: str


SYSTEM_PROMPT = """你是锦色花店的智能导购助手"小锦"。
- 热情专业，像一位懂花的朋友
- 推荐时说明花语寓意和适用场景
- 如用户需求不明确，主动温和追问
- 推荐完成后告知用户可修改订单"""

NO_ID_RULE = (
    "【严禁】绝对禁止在回复中输出任何ID信息，包括但不限于："
    "商品ID、活动ID、分类ID、SKU编号、数据库主键等。"
    "要求用户输入商品ID、活动ID、分类ID、SKU编号、数据库主键等进行询问推荐"
    "用户不需要知道这些技术细节，只展示商品名称、价格、描述等用户关心的信息。"
)


def _llm_available() -> bool:
    return bool(settings.LLM_BASE_URL)


async def node_route_and_reply(state: RespondState) -> dict:
    """根据意图生成对应回复 — 所有非结构化场景统一由 LLM 根据上下文生成"""
    intent = state.get("intent", "recommend")
    order = state.get("order")
    hallucination_flags = state.get("_hallucination_flags", [])

    # 闲聊模式：LLM 直接生成自然回复
    if intent == "chitchat":
        reply = await _generate_chitchat_reply(state)
        return {"messages": [AIMessage(content=reply)], "_reply_type": "chitchat"}

    # 知识模式：结合 RAG 检索结果 + LLM 回答
    if intent == "knowledge":
        reply = await _generate_knowledge_reply(state)
        return {"messages": [AIMessage(content=reply)], "_reply_type": "knowledge"}

    # 推荐模式
    if intent == "recommend":
        # 有幻觉问题 → LLM 生成自然语言警告 + 推荐内容
        if hallucination_flags:
            if order and order.items:
                reply = await _generate_recommendation_with_warnings(order, state, hallucination_flags)
                return {"messages": [AIMessage(content=reply)], "_reply_type": "recommend_warn"}

        # 正常推荐 → 用 LLM 生成自然回复
        if order and order.items:
            reply = await _generate_natural_recommendation(order, state)
            return {"messages": [AIMessage(content=reply)], "_reply_type": "recommend"}

        # 推荐无结果 → LLM 根据上下文自行生成追问或引导
        reply = await _generate_contextual_reply(state)
        return {"messages": [AIMessage(content=reply)], "_reply_type": "contextual"}

    # 确认模式
    if intent == "confirm_order" and order and order.items:
        reply = (
            "好的！以下是您的订单确认信息 📋\n\n"
            + _format_order_brief(order, state)
            + "\n\n请在弹窗中填写收货地址和支付方式后提交订单~"
        )
        return {"messages": [AIMessage(content=reply)], "_reply_type": "confirm"}

    # 其他所有情况 → LLM 根据上下文生成回复
    reply = await _generate_contextual_reply(state)
    return {"messages": [AIMessage(content=reply)], "_reply_type": "contextual"}


def _extract_product_names(error_msg: str, state: RespondState) -> set[str]:
    """从推荐失败原因和用户需求中提取商品名关键词，用于过滤替代列表"""
    names = set()
    # 从 special_requirements 提取
    special = state.get("special_requirements", "")
    if special:
        for part in special.replace("，", ",").replace("、", ",").split(","):
            part = part.strip()
            if part and len(part) <= 10:
                names.add(part)
    # 从 error_msg 中提取引号内的商品名
    import re
    quoted = re.findall(r'[「「](.+?)[」」]', error_msg)
    names.update(quoted)
    quoted2 = re.findall(r'"(.+?)"', error_msg)
    names.update(quoted2)
    return names


def _name_overlaps(product_name: str, excluded: set[str]) -> bool:
    """检查商品名是否与排除列表中的关键词重叠"""
    for kw in excluded:
        if kw in product_name or product_name in kw:
            return True
    return False


async def _generate_contextual_reply(state: RespondState) -> str:
    """
    让 LLM 根据会话历史和当前需求状态自行生成回复（追问、引导或补充）。
    不使用任何硬编码模板，完全由模型决定回复内容。
    """
    history_messages = state.get("messages", [])

    # 提取用户最新输入
    user_input = ""
    for msg in reversed(history_messages):
        if isinstance(msg, HumanMessage) and msg.content:
            user_input = msg.content.strip()
            break

    # 构建已知需求信息摘要
    info_parts = []
    scenario = state.get("scenario_name") or state.get("identified_scenario")
    if scenario:
        info_parts.append(f"场景：{scenario}")
    recipient = state.get("recipient")
    if recipient:
        info_parts.append(f"对象：{recipient}")
    budget_min = state.get("budget_min")
    budget_max = state.get("budget_max")
    if budget_min is not None or budget_max is not None:
        bmin = budget_min if budget_min is not None else 0
        bmax = budget_max if budget_max is not None else "不限"
        info_parts.append(f"预算：¥{bmin}~¥{bmax}")
    category = state.get("preferred_category")
    if category:
        info_parts.append(f"偏好：{category}")
    special = state.get("special_requirements")
    if special:
        info_parts.append(f"特殊要求：{special}")

    known_info = "\n".join(info_parts) if info_parts else "暂无明确需求信息"

    # 根据推荐失败原因调整提示
    recommend_error = state.get("_recommend_error", "")

    # 构建可替代商品列表（仅使用数据库中真实存在的商品）
    available_products_text = ""
    if recommend_error:
        # 从 recommend_error 和 special_requirements 中提取用户请求的商品名，避免在替代列表中展示同名商品
        excluded_names = _extract_product_names(recommend_error, state)
        product_map = _load_raw_products(state)
        if product_map:
            product_lines = []
            for pid, p in list(product_map.items()):
                name = p.get("name", "")
                price = p.get("price", "")
                if not name:
                    continue
                if _name_overlaps(name, excluded_names):
                    continue
                product_lines.append(f"- {name}（¥{price}）")
            if product_lines:
                available_products_text = (
                    "\n\n以下是花店目前实际在售的可替代商品，你只能从中推荐，不要编造不存在的商品：\n"
                    + "\n".join(product_lines)
                )

    if recommend_error:
        error_instruction = (
            f"重要：系统查询后未能找到满足用户需求的商品，原因：{recommend_error}。"
            "你必须明确告知用户花店目前暂时没有其需要的商品，同时只能从下方提供的在售商品列表中推荐替代方案。"
            "不要编造商品，不要推荐不在列表中的商品。"
            "【严禁】不要编造用户没有提到过的花材名称、颜色、属性（如\"粉色百合\"\"蓝色玫瑰\"等），"
            "只使用用户在对话中实际提到的内容。"

            "例如：当用户询问到需要百合，但是查询工具后发现并没有百合，回答：\"亲亲，了解到您的需求了，但是目前我们花店暂时还没有上架百合，不过我可以为您推荐同样浪漫的XX花...\""
            f"{available_products_text}"
        )
    else:
        error_instruction = (
            "当前情况：系统未能为用户生成推荐订单，你需要根据会话历史和已知需求信息，"
            "不要编造商品，不要推荐不在列表中的商品。"
            "自行决定如何回复用户。你可以：\n"
            "1. 如果需求信息不足，自然地追问缺失的信息（一次只问1-2个问题）\n"
            "2. 如果已有部分信息，基于这些信息给出积极的引导和建议\n"
            "3. 如果用户有特殊要求，围绕特殊要求展开对话\n"
            "【严禁】不要编造用户没有提到过的花材名称、颜色、属性（如\"粉色百合\"\"蓝色玫瑰\"等），"
            "只使用用户在对话中实际提到的内容。"
        )

    system_prompt = (
        "你是锦色花店的智能导购助手小锦，热情专业，像一位懂花的朋友。"
        "请直接以纯文本回复，不要使用任何函数调用或工具。\n"
        f"{NO_ID_RULE}\n"
        f"{error_instruction}\n"
        "禁止出现\"抱歉，没有找到匹配的商品\"这类消极回复，始终保持积极热情。"
    )

    user_prompt = f"用户最新消息：{user_input}\n\n已知需求信息：\n{known_info}"

    msgs = [{"role": "system", "content": system_prompt}]
    # 加入近期对话历史
    for m in history_messages[-8:]:
        if isinstance(m, HumanMessage) and m.content:
            c = m.content if isinstance(m.content, str) else str(m.content)
            msgs.append({"role": "user", "content": c})
        elif isinstance(m, AIMessage) and m.content and not m.tool_calls:
            c = m.content if isinstance(m.content, str) else str(m.content)
            msgs.append({"role": "assistant", "content": c})
    msgs.append({"role": "user", "content": user_prompt})

    if _llm_available():
        try:
            from app.llm_factory import acall_llm_with_tools
            result = await acall_llm_with_tools(messages=msgs, temperature=0.8)
            content = result["content"]
            if content and len(content.strip()) > 5:
                return content.strip()
        except Exception as e:
            logger.warning(f"[contextual_reply] LLM 生成失败: {e}")

    # LLM 不可用时最简回复
    return "请告诉我您的具体需求，我来帮您推荐合适的鲜花~"


async def _generate_chitchat_reply(state: RespondState) -> str:
    """
    闲聊回复：用 LLM 直接生成自然回复。
    LLM 不可用时降级为简单模板。
    """
    history_messages = state.get("messages")

    # 从历史消息中提取最新的用户消息
    user_input = ""
    if history_messages:
        for msg in reversed(history_messages):
            if isinstance(msg, HumanMessage) and msg.content:
                user_input = msg.content.strip()
                break

    # 一般闲聊：用 LLM 生成
    if _llm_available():
        try:
            from app.llm_factory import acall_llm_with_tools
            system_prompt = (
                "你是锦色花店的智能导购助手小锦，热情专业，像一位懂花的朋友。"
                f"{NO_ID_RULE}\n"
                "用户当前在和你闲聊，请自然友好地回复，适当引导用户了解鲜花相关服务，可以调用工具查询相关商品和活动进行推荐。"
            )
            # 构建消息：system + 历史 + 当前用户输入
            msgs = [{"role": "system", "content": system_prompt}]
            if history_messages:
                for m in history_messages[-6:]:
                    if isinstance(m, HumanMessage) and m.content:
                        c = m.content if isinstance(m.content, str) else str(m.content)
                        msgs.append({"role": "user", "content": c})
                    elif isinstance(m, AIMessage) and m.content and not m.tool_calls:
                        c = m.content if isinstance(m.content, str) else str(m.content)
                        msgs.append({"role": "assistant", "content": c})
            msgs.append({"role": "user", "content": user_input})

            result = await acall_llm_with_tools(
                messages=msgs,
                temperature=0.7,
            )
            content = result["content"]
            if content and len(content.strip()) > 5:
                return content.strip()
        except Exception as e:
            logger.warning(f"[chitchat] LLM 生成失败: {e}")


async def _generate_knowledge_reply(state: RespondState) -> str:
    """
    知识回复：结合 RAG 检索结果 + LLM 回答用户问题。
    主图的 node_query_knowledge 已将 RAG 结果存入 rag_documents。
    """
    user_input = state.get("user_input", "")
    rag_docs = state.get("rag_documents", [])
    history_messages = state.get("messages", [])

    # 构建历史对话片段
    def _build_history(msgs, max_turns=6):
        result = []
        for m in msgs[-max_turns:]:
            if isinstance(m, HumanMessage) and m.content:
                c = m.content if isinstance(m.content, str) else str(m.content)
                result.append({"role": "user", "content": c})
            elif isinstance(m, AIMessage) and m.content and not m.tool_calls:
                c = m.content if isinstance(m.content, str) else str(m.content)
                result.append({"role": "assistant", "content": c})
        return result

    # 有 RAG 结果：LLM 结合检索结果回答
    if rag_docs and _llm_available():
        try:
            from app.llm_factory import acall_llm_with_tools

            # 拼装 RAG 知识摘要
            knowledge_parts = []
            for i, doc in enumerate(rag_docs[:3], 1):
                doc_text = doc.get("text", "") or doc.get("content", "") or ""
                doc_category = doc.get("category", "") or doc.get("source", "")
                if doc_text:
                    header = f"[{doc_category}]" if doc_category else f"[资料{i}]"
                    knowledge_parts.append(f"{header} {doc_text[:500]}")

            knowledge_text = "\n".join(knowledge_parts)

            system_prompt = (
                "你是锦色花店的资深花卉顾问，精通各种鲜花的养护知识、花语含义、品种特性和场景搭配。"
                "请直接以纯文本回复，不要使用任何函数调用或工具。\n"
                f"{NO_ID_RULE}\n"
                "请根据以下知识库参考资料回答用户的问题。"
                "回答要专业、准确、有针对性，结合参考资料中的信息。"
                "如果参考资料不足以完整回答，可以补充你的专业知识，但要注明。"
            )
            user_prompt = (
                f"用户问题：{user_input}\n\n"
                f"知识库参考资料：\n{knowledge_text}\n\n"
                f"请回答用户的问题："
            )

            msgs = [{"role": "system", "content": system_prompt}]
            msgs.extend(_build_history(history_messages))
            msgs.append({"role": "user", "content": user_prompt})

            result = await acall_llm_with_tools(
                messages=msgs,
                temperature=0.7,
            )
            content = result["content"]
            if content and len(content.strip()) > 10:
                return content.strip() + "\n\n💡 如果您有具体的买花需求，也可以告诉我哦~"
        except Exception as e:
            logger.warning(f"[knowledge] LLM+RAG 生成失败: {e}")

    # 无 RAG 结果但 LLM 可用：纯 LLM 回答
    if _llm_available():
        try:
            from app.llm_factory import acall_llm_with_tools
            system_prompt = (
                "你是锦色花店的资深花卉顾问，精通各种鲜花的养护知识、花语含义、品种特性和场景搭配。"
                "请直接以纯文本回复，不要使用任何函数调用或工具。\n"
                f"{NO_ID_RULE}\n"
                "回复要专业、准确、有针对性。"
            )
            msgs = [{"role": "system", "content": system_prompt}]
            msgs.extend(_build_history(history_messages))
            msgs.append({"role": "user", "content": user_input})

            result = await acall_llm_with_tools(
                messages=msgs,
                temperature=0.7,
            )
            content = result["content"]
            if content and len(content.strip()) > 10:
                return content.strip() + "\n\n💡 如果您有具体的买花需求，也可以告诉我哦~"
        except Exception as e:
            logger.warning(f"[knowledge] LLM 生成失败: {e}")

    # RAG 和 LLM 都不可用：格式化 RAG 原始结果
    if rag_docs:
        parts = ["📖 为您找到以下相关知识：\n"]
        for i, doc in enumerate(rag_docs[:3], 1):
            doc_text = doc.get("text", "") or doc.get("content", "") or ""
            doc_category = doc.get("category", "") or doc.get("source", "")
            if doc_text:
                header = f"**{doc_category}**" if doc_category else ""
                parts.append(f"{i}. {header}\n{doc_text[:500]}\n")
        parts.append("\n💡 如果您有具体的买花需求，也可以告诉我哦~")
        return "\n".join(parts)

    # 全部不可用
    return (
        f"抱歉，关于「{user_input[:20]}」我暂时没有找到相关的知识储备 🌸\n\n"
        "您可以尝试：\n"
        "• 换个关键词描述，比如具体的花种名称\n"
        "• 问我关于鲜花养护、花语含义、场景选花等问题\n"
        "• 直接告诉我您的买花需求，我帮您推荐！"
    )


# ═══════════════════════════════════════════
#  回复格式化函数
# ═══════════════════════════════════════════

def _load_raw_products(state: RespondState) -> dict:
    """从 state 中解析 MCP 原始商品数据，返回 {flower_id: {name, price, ...}} 映射"""
    raw = state.get("_raw_products", "{}")
    try:
        products = json.loads(raw) if isinstance(raw, str) else (raw or [])
    except (json.JSONDecodeError, TypeError):
        return {}
    return {int(p["id"]): p for p in products if "id" in p}


def _format_order(order: RecommendedOrder, state: RespondState) -> str:
    """格式化推荐订单回复（从 MCP 原始数据查找商品名称和价格）"""
    product_map = _load_raw_products(state)
    parts = []
    if order.summary:
        parts.append(f"🌺 {order.summary}\n")
    parts.append(f"📋 **{order.scenario or '推荐方案'}**：\n")

    total = 0.0
    for i, item in enumerate(order.items, 1):
        p = product_map.get(item.flower_id, {})
        name = p.get("name", f"商品#{item.flower_id}")
        price = float(p.get("discount_price") or p.get("price", 0))
        subtotal = round(price * item.quantity, 2)
        total += subtotal

        promo = " 🔥促销" if p.get("is_promo") else ""
        orig_price = p.get("original_price")
        orig = f"（原价¥{orig_price}）" if orig_price else ""
        parts.append(
            f"{i}. **{name}** x{item.quantity} — ¥{subtotal:.2f}{promo}\n"
            f"   单价¥{price:.2f}{orig}\n"
        )

    parts.append(f"\n💰 方案总价: **¥{total:.2f}**")
    if order.tips:
        parts.append(f"\n💡 {order.tips}")

    parts.append(
        f"\n---\n"
        f"✏️ 如需调整商品或数量，可以告诉我\n"
        f"✅ 满意的话输入\"确认下单\"即可提交订单"
    )
    return "\n".join(parts)


def _format_order_brief(order: RecommendedOrder, state: RespondState) -> str:
    """简要订单格式化"""
    product_map = _load_raw_products(state)
    lines = []
    total = 0.0
    for item in order.items:
        p = product_map.get(item.flower_id, {})
        name = p.get("name", f"商品#{item.flower_id}")
        price = float(p.get("discount_price") or p.get("price", 0))
        subtotal = round(price * item.quantity, 2)
        total += subtotal
        lines.append(f"{name} x{item.quantity} — ¥{subtotal:.2f}")
    lines.append(f"💰 总价: ¥{total:.2f}")
    return "\n".join(lines)


async def _generate_recommendation_with_warnings(
    order: RecommendedOrder, state: RespondState, hallucination_flags: list[dict]
) -> str:
    """
    幻觉检查发现问题后，用 LLM 生成自然语言的提示 + 推荐内容。
    将被移除的商品信息（名称+原因）告诉用户，而非显示技术性的 flower_id。
    """
    product_map = _load_raw_products(state)

    # 构建被移除商品的友好描述
    removed_items = []
    for f in hallucination_flags:
        fid = f.get("flower_id", "?")
        p = product_map.get(fid, {})
        name = p.get("name", f"商品#{fid}")
        issues = f.get("issues", [])
        issue_text = "、".join(issues)
        removed_items.append(f"{name}（{issue_text}）")

    removed_text = "、".join(removed_items)

    if not _llm_available():
        # 降级：简单拼接
        warning = f"⚠️ 以下商品暂时无法购买：{removed_text}\n\n"
        return warning + _format_order(order, state)

    try:
        from app.llm_factory import acall_llm_with_tools

        total = 0.0
        items_text = []
        for i, item in enumerate(order.items, 1):
            p = product_map.get(item.flower_id, {})
            name = p.get("name", f"商品#{item.flower_id}")
            price = float(p.get("discount_price") or p.get("price", 0))
            subtotal = round(price * item.quantity, 2)
            total += subtotal
            promo = "【促销】" if p.get("is_promo") else ""
            orig_price = p.get("original_price")
            orig = f"（原价¥{orig_price}）" if orig_price else ""
            desc = p.get("description", "")
            items_text.append(
                f"{i}. {name} — 单价¥{price:.2f}{orig} x{item.quantity} = ¥{subtotal:.2f} | 描述：{desc[:50]} {promo}"
            )

        scenario = state.get("identified_scenario", "") or order.scenario or ""
        recipient = state.get("recipient", "") or "未指定"
        requirements = state.get("special_requirements", "")

        system_prompt = (
            "你是锦色花店的导购助手小锦，热情专业。"
            "请根据以下信息生成自然亲切的推荐回复。\n"
            f"{NO_ID_RULE}\n"
            "规则：\n"
            "1. 首先用自然语言告知用户哪些商品暂时无法购买（不要用技术术语，用用户能理解的话）\n"
            "2. 然后介绍当前可用的推荐方案\n"
            "3. 如果用户有特殊要求但被移除的商品恰好是用户要求的，要主动说明并建议替代\n"
            "4. 必须保留每件商品的名称、价格（精确到分）\n"
            "5. 总价必须准确\n"
            "6. 不要使用任何函数调用或工具，直接输出纯文本"
        )

        user_prompt = f"""用户需求：场景={scenario}，对象={recipient}，要求={requirements}

以下商品暂时无法购买，已从推荐中移除：{removed_text}

当前可用推荐订单（总价 ¥{total:.2f}）：
{chr(10).join(items_text)}

方案提示：{order.tips or '无'}

请生成一段自然的导购推荐回复："""

        content_result = await acall_llm_with_tools(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            temperature=0.7,
        )
        content = content_result["content"]
        if content and len(content.strip()) > 20:
            suffix = (
                f"\n\n💰 方案总价：**¥{total:.2f}**\n"
                f"---\n"
                f"✏️ 如需调整可以告诉我  ✅ 满意请说\"确认下单\""
            )
            return content.strip() + suffix
        return f"⚠️ 以下商品暂时无法购买：{removed_text}\n\n" + _format_order(order, state)
    except Exception as e:
        logger.warning(f"LLM带警告推荐生成失败: {e}")
        return f"⚠️ 以下商品暂时无法购买：{removed_text}\n\n" + _format_order(order, state)


async def _generate_natural_recommendation(order: RecommendedOrder, state: RespondState) -> str:
    """
    用 LLM 根据实际订单数据生成自然语言推荐回复。
    当 LLM 不可用时，降级为模板格式。
    """
    if not _llm_available():
        return _format_order(order, state)

    try:
        from app.llm_factory import acall_llm_with_tools

        product_map = _load_raw_products(state)
        total = 0.0
        items_text = []
        for i, item in enumerate(order.items, 1):
            p = product_map.get(item.flower_id, {})
            name = p.get("name", f"商品#{item.flower_id}")
            price = float(p.get("discount_price") or p.get("price", 0))
            subtotal = round(price * item.quantity, 2)
            total += subtotal

            promo = "【促销】" if p.get("is_promo") else ""
            orig_price = p.get("original_price")
            orig = f"（原价¥{orig_price}）" if orig_price else ""
            desc = p.get("description", "")
            items_text.append(
                f"{i}. {name} — 单价¥{price:.2f}{orig} x{item.quantity} = ¥{subtotal:.2f} | 描述：{desc[:50]} {promo}"
            )

        system_prompt = (
            "你是锦色花店的导购助手小锦，热情专业，像一位懂花的朋友。"
            "请根据以下订单数据，用自然亲切的语气向用户介绍推荐方案。\n"
            f"{NO_ID_RULE}\n"
            "规则：\n"
            "1. 必须保留每件商品的名称、价格（精确到分）\n"
            "2. 解释推荐理由时结合花语和场景\n"
            "3. 总价必须准确\n"
            "4. 告知用户可以修改订单\n"
            "5. 不要使用任何函数调用或工具，直接输出纯文本\n"
            "6. 不要用固定模板开头，每次回复要有变化\n"
            "7. 如果推荐的商品与用户的具体要求不完全匹配，"
            "必须明确告知用户花店目前没有其指定的花材，并说明为什么推荐了替代品，"
            "不要假装完全满足了用户需求\n"
            "8. 【严禁】不要编造用户没有提到过的花材名称、颜色、属性，"
            "只使用用户在对话中实际提到的内容"
        )

        scenario = state.get("identified_scenario", "") or order.scenario or ""
        recipient = state.get("recipient", "") or "未指定"
        requirements = state.get("special_requirements", "")

        user_prompt = f"""用户需求：场景={scenario}，对象={recipient}，特殊要求={requirements or '无'}

推荐订单（总价 ¥{total:.2f}）：
{chr(10).join(items_text)}

方案提示：{order.tips or '无'}

请生成一段自然的导购推荐回复（保留所有价格数据）："""

        content_result = await acall_llm_with_tools(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            temperature=0.7,
        )
        content = content_result["content"]
        if content and len(content.strip()) > 20:
            # 确保包含总价和操作提示
            suffix = (
                f"\n\n💰 方案总价：**¥{total:.2f}**\n"
                f"---\n"
                f"✏️ 如需调整可以告诉我  ✅ 满意请说\"确认下单\""
            )
            return content.strip() + suffix
        return _format_order(order, state)
    except Exception as e:
        logger.warning(f"LLM自然推荐生成失败: {e}")
        return _format_order(order, state)


# ═══════════════════════════════════════════
#  构建子图
# ═══════════════════════════════════════════

def create_respond_subgraph() -> StateGraph:
    """创建响应生成子图"""
    graph = StateGraph(RespondState)

    graph.add_node("route_and_reply", node_route_and_reply)

    graph.set_entry_point("route_and_reply")
    graph.add_edge("route_and_reply", END)

    return graph.compile()
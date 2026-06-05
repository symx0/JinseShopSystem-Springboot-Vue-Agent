import json
import logging
from decimal import Decimal

from langchain_core.messages import AIMessage, SystemMessage
from langchain_openai import ChatOpenAI

from app.config import settings
from app.graph.state import ConversationState
from app.knowledge import product_kb, scenario_kb
from app.models.schemas import RecommendationItem, RecommendationPlan

logger = logging.getLogger(__name__)

# 系统提示词
SYSTEM_PROMPT = """你是锦色花店的智能导购助手，你的职责是根据用户的需求场景推荐合适的鲜花商品。

## 你的工作流程：
1. **理解需求**：分析用户描述的购买场景、送礼对象、预算等
2. **追问澄清**：如果信息不够明确，主动提问以更好地了解需求
3. **推荐商品**：根据需求从知识库中匹配最合适的商品
4. **给出方案**：提供完整的购买方案，包含推荐理由

## 你需要了解的信息：
- 购买场景（表白、送礼、装饰、造景等）
- 送礼对象（恋人、母亲、朋友、同事等）
- 预算范围
- 特殊偏好（颜色、花语、是否需要盆栽等）

## 回复风格：
- 热情专业，像一位懂花的朋友
- 推荐时说明花语和寓意
- 给出具体的购买建议和搭配方案
- 如果用户需求不明确，温和地追问

## 注意事项：
- 只推荐知识库中存在的商品
- 如实告知价格和特点，不夸大
- 如果没有完全匹配的商品，推荐最接近的并说明原因
"""

CLARIFY_PROMPT = """根据以下对话历史和当前用户输入，判断是否需要追问更多信息。

当前已识别的信息：
- 场景：{scenario}
- 场景置信度：{confidence}
- 关键词：{keywords}
- 预算：{budget}
- 偏好分类：{category}
- 送礼对象：{recipient}

如果场景不明确（置信度低于0.5）或缺少关键信息（如送礼对象、预算），请生成一个简短的追问。
如果信息已经足够，请回复"信息充足"。

追问要求：
- 一次只问1-2个问题
- 语气自然友好
- 给出选项帮助用户回答
"""

ANALYZE_PROMPT = """分析用户输入，提取购买需求信息。

用户输入：{user_input}

请以JSON格式输出以下信息：
{{
    "scenario": "最匹配的场景ID（从以下选项中选择：confession/valentine/mother_day/graduation/housewarming/business/home_decor/garden/spring_festival/visit_patient），如果无法判断则为null",
    "keywords": ["提取的关键词列表"],
    "budget_min": 预算下限（数字，未提及则为0），
    "budget_max": 预算上限（数字，未提及则为9999），
    "recipient": "送礼对象描述，未提及则为null",
    "category_preference": "偏好分类（花束/盆栽/多肉/园林造景），未提及则为null",
    "special_requirements": "特殊要求描述，未提及则为null"
}}

只输出JSON，不要其他内容。"""

RECOMMEND_PROMPT = """根据用户需求，从以下候选商品中选择最合适的推荐。

用户需求：
- 场景：{scenario}
- 预算：{budget}
- 送礼对象：{recipient}
- 偏好分类：{category}
- 特殊要求：{requirements}

候选商品：
{candidates}

请选择3-5个最合适的商品，以JSON格式输出：
{{
    "recommendations": [
        {{
            "product_id": 商品ID,
            "reason": "推荐理由（包含花语寓意、适合场景等）",
            "match_score": 匹配度评分0-1
        }}
    ],
    "summary": "方案概述",
    "tips": "额外建议"
}}

只输出JSON，不要其他内容。"""


def _get_llm() -> ChatOpenAI:
    """获取LLM实例"""
    return ChatOpenAI(
        api_key=settings.LLM_API_KEY,
        base_url=settings.LLM_BASE_URL,
        model=settings.LLM_MODEL,
        temperature=0.7,
    )


def analyze_need(state: ConversationState) -> dict:
    """需求分析节点：分析用户输入，提取场景和关键词"""
    user_input = state.get("user_input", "")
    messages = state.get("messages", [])

    # 先用知识库做关键词匹配
    scenario_matches = scenario_kb.match_scenario(user_input)
    keyword_results = product_kb.search_products(user_input)

    # 基于知识库的初步分析
    identified_scenario = None
    scenario_confidence = 0.0
    extracted_keywords = []

    if scenario_matches:
        best_scenario, match_count = scenario_matches[0]
        identified_scenario = best_scenario.id
        scenario_confidence = min(match_count / 2.0, 1.0)  # 匹配2个关键词即为高置信
        extracted_keywords = best_scenario.keywords[:match_count]

    # 如果有LLM，用LLM做更精细的分析
    if settings.LLM_API_KEY:
        try:
            llm = _get_llm()
            prompt = ANALYZE_PROMPT.format(user_input=user_input)
            response = llm.invoke([SystemMessage(content=prompt)])
            analysis = json.loads(response.content)

            # LLM分析结果覆盖知识库结果（LLM更精确）
            if analysis.get("scenario"):
                identified_scenario = analysis["scenario"]
                scenario_confidence = 0.8
            if analysis.get("keywords"):
                extracted_keywords = analysis["keywords"]

            budget_range = (
                analysis.get("budget_min", 0),
                analysis.get("budget_max", 9999),
            )
            if budget_range == (0, 9999):
                budget_range = None

            return {
                "identified_scenario": identified_scenario,
                "scenario_confidence": scenario_confidence,
                "extracted_keywords": extracted_keywords,
                "budget_range": state.get("budget_range") or budget_range,
                "preferred_category": state.get("preferred_category") or analysis.get("category_preference"),
                "recipient": state.get("recipient") or analysis.get("recipient"),
                "special_requirements": state.get("special_requirements") or analysis.get("special_requirements"),
                "current_step": "clarify",
            }
        except Exception as e:
            logger.warning(f"LLM分析失败，回退到知识库分析: {e}")

    # 知识库分析结果
    return {
        "identified_scenario": identified_scenario,
        "scenario_confidence": scenario_confidence,
        "extracted_keywords": extracted_keywords,
        "current_step": "clarify",
    }


def clarify_need(state: ConversationState) -> dict:
    """追问澄清节点：判断是否需要更多信息，生成追问"""
    scenario = state.get("identified_scenario")
    confidence = state.get("scenario_confidence", 0)
    budget = state.get("budget_range")
    recipient = state.get("recipient")
    category = state.get("preferred_category")

    # 判断是否需要更多信息
    need_more = False
    follow_up = None

    if not scenario or confidence < 0.5:
        need_more = True
        follow_up = "请问您是为什么场景选购鲜花呢？比如：送恋人、送母亲、家居装饰、庭院造景等？"
    elif not recipient and scenario not in ("home_decor", "garden"):
        need_more = True
        follow_up = "请问是送给谁的呢？这样我可以推荐更合适的花语寓意。"
    elif not budget and not category:
        need_more = True
        follow_up = "您有预算范围或偏好的类型吗？比如花束、盆栽、多肉等？"

    # 如果有LLM，让LLM生成更自然的追问
    if need_more and settings.LLM_API_KEY:
        try:
            llm = _get_llm()
            prompt = CLARIFY_PROMPT.format(
                scenario=scenario or "未识别",
                confidence=confidence,
                keywords=state.get("extracted_keywords", []),
                budget=f"{budget[0]}-{budget[1]}元" if budget else "未明确",
                category=category or "未明确",
                recipient=recipient or "未明确",
            )
            response = llm.invoke([SystemMessage(content=prompt)])
            content = response.content.strip()
            if content != "信息充足":
                follow_up = content
            else:
                need_more = False
        except Exception as e:
            logger.warning(f"LLM追问生成失败，使用默认追问: {e}")

    if need_more:
        return {
            "need_more_info": True,
            "follow_up_question": follow_up,
            "current_step": "respond",
        }

    return {
        "need_more_info": False,
        "current_step": "recommend",
    }


def recommend_products(state: ConversationState) -> dict:
    """商品推荐节点：根据需求从知识库匹配商品"""
    scenario_id = state.get("identified_scenario")
    budget = state.get("budget_range")
    category = state.get("preferred_category")
    keywords = state.get("extracted_keywords", [])

    # 从知识库获取候选商品
    candidates = []

    # 1. 按场景获取
    if scenario_id:
        candidates.extend(product_kb.get_products_by_scenario(scenario_id))

    # 2. 按关键词搜索补充
    for kw in keywords:
        for p in product_kb.search_products(kw):
            if p not in candidates:
                candidates.append(p)

    # 3. 按分类筛选
    if category:
        category_products = product_kb.get_products_by_category(category)
        for p in category_products:
            if p not in candidates:
                candidates.append(p)

    # 4. 如果没有匹配，获取全部在售商品
    if not candidates:
        candidates = product_kb.get_all_products()

    # 按预算过滤
    if budget:
        min_price, max_price = budget
        candidates = [p for p in candidates if min_price <= float(p.price) <= max_price]

    # 去重
    seen = set()
    unique_candidates = []
    for p in candidates:
        if p.id not in seen:
            seen.add(p.id)
            unique_candidates.append(p)

    # 如果有LLM，让LLM做精细推荐
    if settings.LLM_API_KEY and unique_candidates:
        try:
            llm = _get_llm()
            scenario_info = scenario_kb.get_scenario_by_id(scenario_id) if scenario_id else None
            candidates_str = "\n".join(
                f"- ID:{p.id} 名称:{p.name} 分类:{p.category} 价格:{p.price}元 "
                f"描述:{p.description} 标签:{','.join(p.tags)} 场景:{','.join(p.applicable_scenarios)}"
                for p in unique_candidates
            )
            prompt = RECOMMEND_PROMPT.format(
                scenario=f"{scenario_info.name} - {scenario_info.description}" if scenario_info else "通用",
                budget=f"{budget[0]}-{budget[1]}元" if budget else "不限",
                recipient=state.get("recipient") or "未明确",
                category=category or "不限",
                requirements=state.get("special_requirements") or "无",
                candidates=candidates_str,
            )
            response = llm.invoke([SystemMessage(content=prompt)])
            rec_data = json.loads(response.content)

            # 构建推荐方案
            items = []
            for rec in rec_data.get("recommendations", []):
                product = product_kb.get_product_by_id(rec["product_id"])
                if product:
                    items.append(RecommendationItem(
                        product=product,
                        reason=rec["reason"],
                        match_score=rec.get("match_score", 0.8),
                    ))

            total = sum(float(item.product.price) for item in items) if items else None

            plan = RecommendationPlan(
                scenario=scenario_info.name if scenario_info else "通用推荐",
                summary=rec_data.get("summary", ""),
                items=items,
                total_price=Decimal(str(total)) if total else None,
                tips=rec_data.get("tips", ""),
            )

            return {"plan": plan, "current_step": "generate"}

        except Exception as e:
            logger.warning(f"LLM推荐失败，回退到规则推荐: {e}")

    # 规则推荐：按匹配度排序
    items = []
    for p in unique_candidates[:5]:
        score = 0.5
        if scenario_id and scenario_id in p.applicable_scenarios:
            score += 0.3
        if category and p.category == category:
            score += 0.2
        items.append(RecommendationItem(
            product=p,
            reason=f"{p.name}适合{state.get('identified_scenario', '通用')}场景，{p.description}",
            match_score=min(score, 1.0),
        ))

    total = sum(float(item.product.price) for item in items) if items else None
    scenario_info = scenario_kb.get_scenario_by_id(scenario_id) if scenario_id else None

    plan = RecommendationPlan(
        scenario=scenario_info.name if scenario_info else "通用推荐",
        summary=f"根据您的需求，为您推荐以下商品",
        items=items,
        total_price=Decimal(str(total)) if total else None,
        tips=scenario_info.tips if scenario_info else "",
    )

    return {"plan": plan, "current_step": "generate"}


def generate_response(state: ConversationState) -> dict:
    """响应生成节点：生成最终回复"""
    need_more = state.get("need_more_info", False)
    follow_up = state.get("follow_up_question")
    plan = state.get("plan")

    if need_more and follow_up:
        # 追问模式
        reply = follow_up
    elif plan and plan.items:
        # 推荐模式
        reply_parts = [f"根据您的需求，为您推荐以下方案：\n"]
        for i, item in enumerate(plan.items, 1):
            reply_parts.append(
                f"{i}. **{item.product.name}** - {item.product.price}元\n"
                f"   分类：{item.product.category}\n"
                f"   推荐理由：{item.reason}\n"
            )
        if plan.tips:
            reply_parts.append(f"\n小贴士：{plan.tips}")
        if plan.total_price:
            reply_parts.append(f"\n方案总价：{plan.total_price}元")
        reply = "\n".join(reply_parts)
    else:
        reply = "抱歉，暂时没有找到合适的商品推荐。您可以告诉我更具体的需求吗？比如购买场景、预算范围等？"

    # 如果有LLM，让回复更自然
    if settings.LLM_API_KEY and not need_more:
        try:
            llm = _get_llm()
            messages = list(state.get("messages", []))
            messages.append(SystemMessage(content=SYSTEM_PROMPT))
            messages.append(SystemMessage(content=f"请根据以下推荐方案，用自然友好的语气给用户回复：\n{reply}"))
            response = llm.invoke(messages)
            reply = response.content
        except Exception as e:
            logger.warning(f"LLM回复生成失败，使用模板回复: {e}")

    return {
        "messages": [AIMessage(content=reply)],
        "current_step": "end",
    }

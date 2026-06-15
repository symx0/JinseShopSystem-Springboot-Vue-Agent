"""
子图3: 商品推荐 (Recommend Subgraph)

功能:
  1. 通过MCP从MySQL数据库查询商品和活动
  2. LLM智能匹配，基于RAG知识库增强推荐质量
  3. 输出 {flower_id, quantity} 列表（前端根据ID从Spring Boot获取详情）

内部流程:
  fetch_data（MCP查询）→ llm_recommend（LLM推荐）→ END

输出:
  - order: RecommendedOrder { items: [{flower_id, quantity}], scenario, summary, tips }
"""
import json
import logging
from typing import TypedDict, Optional

from langgraph.graph import StateGraph, END

from app.config import settings
from app.model_manager import model_manager
from app.models.schemas import OrderItem, RecommendedOrder

logger = logging.getLogger(__name__)


class RecommendState(TypedDict, total=False):
    """推荐子图内部状态"""
    # 输入
    identified_scenario: Optional[str]
    scenario_confidence: float
    extracted_keywords: list[str]
    budget_min: Optional[float]
    budget_max: Optional[float]
    preferred_category: Optional[str]
    recipient: Optional[str]
    special_requirements: Optional[str]

    # 中间数据
    _raw_products_json: Optional[str]
    _raw_activities_json: Optional[str]
    _rag_results_json: Optional[str]
    _recommend_error: Optional[str]

    # 输出
    order: Optional[RecommendedOrder]


RECOMMEND_PROMPT = """/no_think
你是一个花店导购助手。请直接以纯文本JSON格式回复，不要使用任何函数调用或工具。

根据以下信息为用户推荐商品方案。

用户需求:
- 场景: {scenario}
- 预算: {budget}
- 送礼对象: {recipient}
- 偏好: {category}
- 要求: {requirements}

候选商品（来自数据库，含实时价格和活动）:
{candidates}

当前活动:
{activities}

RAG知识库参考:
{rag_info}

请从候选商品中选择3-5个最合适的组成推荐方案。优先选有促销活动的商品。
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
3. 只输出JSON，不要输出任何解释文字"""


def _llm_available() -> bool:
    return bool(settings.LLM_BASE_URL)


async def node_fetch_data(state: RecommendState) -> dict:
    """从MCP查询商品和活动数据"""
    from app.mcp_client import get_mcp_client

    client = get_mcp_client()
    products_json = "[]"
    activities_json = "[]"
    rag_json = "[]"

    if client.is_available:
        try:
            # 并行获取
            products_json = await client.call_tool("tool_get_all_flowers_with_promos")
        except Exception as e:
            logger.warning(f"MCP商品查询失败: {e}")

        try:
            activities_json = await client.call_tool("tool_get_ongoing_activities")
        except Exception as e:
            logger.warning(f"MCP活动查询失败: {e}")

        # RAG知识库检索
        try:
            kw_str = " ".join(state.get("extracted_keywords", [])) or state.get("identified_scenario", "") or "鲜花推荐"
            rag_json = await client.call_tool("tool_rag_search", {"query": kw_str, "top_k": model_manager.rag_initial_top_k})
            # BGE 重排序
            if rag_json:
                try:
                    parsed = json.loads(rag_json) if isinstance(rag_json, str) else rag_json
                    if isinstance(parsed, list) and len(parsed) > 1:
                        from app.llm_factory import rerank_rag_docs
                        reranked = await rerank_rag_docs(kw_str, parsed, top_k=model_manager.rag_top_k)
                        rag_json = json.dumps(reranked, ensure_ascii=False)
                except Exception as e:
                    logger.warning(f"RAG重排序失败: {e}")
        except Exception as e:
            logger.warning(f"RAG检索失败: {e}")

    return {
        "_raw_products_json": products_json,
        "_raw_activities_json": activities_json,
        "_rag_results_json": rag_json,
    }


async def node_llm_recommend(state: RecommendState) -> dict:
    """LLM智能推荐"""
    if not _llm_available():
        return {"_recommend_error": "LLM未配置，无法进行智能推荐。请在设置中配置LLM模型。"}

    products_json = state.get("_raw_products_json", "[]")
    activities_json = state.get("_raw_activities_json", "[]")
    rag_json = state.get("_rag_results_json", "[]")

    try:
        all_products = json.loads(products_json) if isinstance(products_json, str) else products_json
        activities = json.loads(activities_json) if isinstance(activities_json, str) else activities_json
        rag_results = json.loads(rag_json) if isinstance(rag_json, str) else rag_json
    except (json.JSONDecodeError, TypeError):
        return {"_recommend_error": "MCP返回数据格式异常，请稍后重试。"}

    if not all_products:
        return {"_recommend_error": "当前暂无可用商品数据，请联系管理员。"}

    try:
        from app.llm_factory import call_llm

        # 构建精简候选文本
        candidate_lines = []
        for p in all_products[:40]:
            promo = "🔥活动" if p.get("is_promo") else ""
            orig = f"原价{p['original_price']}" if p.get("original_price") else ""
            candidate_lines.append(
                f"ID:{p['id']} | {p['name']} | {p.get('category_name','')} | "
                f"¥{p['price']} {orig} | {p.get('description','')[:40]} {promo}"
            )

        # RAG知识摘要
        rag_summary = ""
        if isinstance(rag_results, list) and rag_results:
            rag_summary = "\n".join(
                f"[{r.get('category','')}] {r.get('text','')[:200]}" for r in rag_results[:3]
            )

        system_prompt = RECOMMEND_PROMPT.format(
            scenario=state.get("identified_scenario", "通用推荐"),
            budget=f"¥{state['budget_min']}~¥{state['budget_max']}" if state.get("budget_min") is not None or state.get("budget_max") is not None else "不限",
            recipient=state.get("recipient", "未明确"),
            category=state.get("preferred_category", "不限"),
            requirements=state.get("special_requirements", "无"),
            candidates="\n".join(candidate_lines),
            activities=json.dumps(activities, ensure_ascii=False)[:500] if activities else "无",
            rag_info=rag_summary or "无",
        )

        content = call_llm(system_prompt=system_prompt, user_message="请根据以上信息进行推荐", temperature=0.7)
        rec_data = _parse_json(content)

        # 构建 RecommendedOrder（仅 flower_id + quantity）
        items = []
        for rec in rec_data.get("recommendations", []):
            pid = rec.get("product_id")
            if pid is None:
                continue
            # 验证商品存在于候选列表中
            if not any(p["id"] == pid for p in all_products):
                logger.warning(f"LLM推荐了不存在的商品ID: {pid}，已跳过")
                continue
            qty = max(1, min(rec.get("quantity", 1), 99))
            items.append(OrderItem(flower_id=pid, quantity=qty))

        # 所有 LLM 推荐都被过滤 → 关键词匹配降级兜底
        if not items and all_products:
            logger.warning("LLM推荐的product_id全部无效，启用关键词匹配降级")
            items = _keyword_fallback(state, all_products)

        order = RecommendedOrder(
            items=items,
            scenario=rec_data.get("scenario_name", state.get("identified_scenario", "个性化推荐")),
            summary=rec_data.get("summary", ""),
            tips=rec_data.get("tips", ""),
        )

        return {"order": order}

    except Exception as e:
        logger.warning(f"LLM推荐失败: {e}")
        return {"_recommend_error": f"智能推荐暂时不可用，请稍后重试。"}


def _keyword_fallback(state: dict, all_products: list[dict]) -> list:
    """
    LLM推荐无效时的关键词匹配降级。
    根据场景名、提取关键词、偏好类别匹配候选商品。
    """
    scenario = (state.get("identified_scenario") or "").lower()
    keywords = [k.lower() for k in state.get("extracted_keywords", [])]
    category = (state.get("preferred_category") or "").lower()

    # 场景→关键词映射
    scenario_map = {
        "valentine": ["玫瑰", "红", "浪漫", "心动", "爱情"],
        "confession": ["玫瑰", "表白", "浪漫", "心动"],
        "mother_day": ["康乃馨", "温馨", "母亲", "感恩"],
        "birthday": ["生日", "祝福", "快乐", "阳光"],
        "anniversary": ["玫瑰", "纪念", "浪漫", "百合"],
        "graduation": ["向日葵", "阳光", "前程", "希望"],
        "apology": ["道歉", "百合", "淡雅"],
        "home_decor": ["多肉", "盆栽", "绿植", "清新"],
        "wedding": ["玫瑰", "百合", "白", "浪漫", "幸福"],
    }
    extra_kw = scenario_map.get(scenario, [])

    scored = []
    for p in all_products:
        pid = p.get("id")
        name = (p.get("name") or "").lower()
        desc = (p.get("description") or "").lower()
        cat = (p.get("category_name") or "").lower()

        score = 0
        for kw in keywords + extra_kw:
            kw_lower = kw.lower()
            if kw_lower in name:
                score += 10
            elif kw_lower in desc:
                score += 5
            elif kw_lower in cat:
                score += 3

        # 促销加分
        if p.get("is_promo"):
            score += 2
        # 类别偏好加分
        if category and category in cat:
            score += 5

        if score > 0:
            scored.append((pid, score))

    # 按分数降序取前5
    scored.sort(key=lambda x: x[1], reverse=True)
    top = scored[:5] if scored else all_products[:5]

    items = []
    for pid, _ in top:
        items.append(OrderItem(flower_id=pid if isinstance(pid, int) else int(pid), quantity=1))

    logger.info(f"[keyword_fallback] 关键词匹配到 {len(items)} 款商品")
    return items


def _parse_json(text: str) -> dict:
    import re
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

def create_recommend_subgraph() -> StateGraph:
    """
    创建商品推荐子图

    内部流程:
      fetch_data（MCP查询）→ llm_recommend（LLM推荐）→ END
    """
    graph = StateGraph(RecommendState)

    graph.add_node("fetch_data", node_fetch_data)
    graph.add_node("llm_recommend", node_llm_recommend)

    graph.set_entry_point("fetch_data")
    graph.add_edge("fetch_data", "llm_recommend")
    graph.add_edge("llm_recommend", END)

    return graph.compile()
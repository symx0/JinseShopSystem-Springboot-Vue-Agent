"""
子图1: 需求分析 (Analyze Subgraph)

功能:
  1. 接收用户输入，识别意图类型
  2. 提取场景、关键词、预算、送礼对象等结构化信息
  3. 输出分析结果供后续子图使用

全部由 LLM 判断，无任何硬编码关键词。
"""
import json
import logging
import re
from typing import TypedDict, Optional

from langgraph.graph import StateGraph, END

from app.config import settings

logger = logging.getLogger(__name__)

# ═══════════════════════════════════════════
#  子图状态 (Subgraph State)
# ═══════════════════════════════════════════

class AnalyzeState(TypedDict, total=False):
    """需求分析子图的内部状态"""
    user_input: str
    # 输出
    identified_scenario: Optional[str]
    scenario_confidence: float
    extracted_keywords: list[str]
    budget_min: Optional[float]
    budget_max: Optional[float]
    preferred_category: Optional[str]
    recipient: Optional[str]
    special_requirements: Optional[str]
    intent: str


# ═══════════════════════════════════════════
#  提示词
# ═══════════════════════════════════════════

ANALYZE_PROMPT = """/no_think
你是一个数据分析助手。请直接以纯文本JSON格式回复，不要使用任何函数调用或工具。

分析以下用户消息，提取购买需求信息。

用户消息: {user_input}

请以JSON格式输出（只输出JSON）：
{{
    "intent": "recommend" | "knowledge" | "greeting" | "modify_order" | "confirm_order",
    "scenario": "场景英文ID (如: confession/valentine/mother_day/graduation/housewarming/business/home_decor/garden/spring_festival/visit_patient/birthday/anniversary) 或 null",
    "keywords": ["关键词列表"],
    "budget_min": 预算下限(0表示未提及),
    "budget_max": 预算上限(9999表示未提及),
    "recipient": "送礼对象或null",
    "category_preference": "偏好分类(花束/盆栽/多肉/园林造景)或null",
    "special_requirements": "特殊要求或null"
}}"""


# ═══════════════════════════════════════════
#  节点函数
# ═══════════════════════════════════════════

def _llm_available() -> bool:
    return bool(settings.LLM_BASE_URL)


def node_llm_analyze(state: AnalyzeState) -> dict:
    """
    使用 LLM 进行意图分析和需求提取。
    不依赖任何硬编码关键词，完全由模型判断。
    当 LLM 不可用时，返回空分析（后续由 clarify 子图接手）。
    """
    user_input = state.get("user_input", "")

    if not _llm_available():
        logger.warning("[analyze] LLM 不可用，跳过分析")
        return {
            "identified_scenario": None,
            "scenario_confidence": 0.0,
            "extracted_keywords": [],
            "intent": "recommend",
        }

    try:
        from app.llm_factory import call_llm
        logger.info(f"[analyze] LLM配置: base_url={settings.LLM_BASE_URL}, model={settings.LLM_MODEL}")
        prompt = ANALYZE_PROMPT.format(user_input=user_input)
        logger.info(f"[analyze] 即将调用LLM, prompt长度={len(prompt)}")
        content = call_llm(system_prompt=prompt, user_message=user_input, temperature=0.3)
        logger.info(f"[analyze] LLM返回内容(前200字): {content[:200] if content else 'EMPTY'}")

        if not content:
            logger.warning("[analyze] LLM 返回空内容")
            return {
                "identified_scenario": None,
                "scenario_confidence": 0.0,
                "extracted_keywords": [],
                "intent": "recommend",
            }

        analysis = _parse_json(content)

        bmin = analysis.get("budget_min", 0)
        bmax = analysis.get("budget_max", 9999)

        result = {
            "identified_scenario": analysis.get("scenario"),
            "scenario_confidence": 0.8 if analysis.get("scenario") else 0.2,
            "extracted_keywords": analysis.get("keywords", []),
            "budget_min": float(bmin) if bmin > 0 else None,
            "budget_max": float(bmax) if bmax < 9999 else None,
            "preferred_category": analysis.get("category_preference"),
            "recipient": analysis.get("recipient"),
            "special_requirements": analysis.get("special_requirements"),
            "intent": analysis.get("intent", "recommend"),
        }
        logger.info(
            f"[analyze] 结果: scenario={result['identified_scenario']}, "
            f"recipient={result['recipient']}, budget_min={result['budget_min']}, budget_max={result['budget_max']}, "
            f"category={result['preferred_category']}"
        )
        return result
    except Exception as e:
        import traceback
        logger.warning(f"[analyze] LLM 分析失败: {e}")
        logger.error(f"[analyze] 完整堆栈:\n{traceback.format_exc()}")
        return {
            "identified_scenario": None,
            "scenario_confidence": 0.0,
            "extracted_keywords": [],
            "intent": "recommend",
        }


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

def create_analyze_subgraph() -> StateGraph:
    """创建需求分析子图（LLM only，无规则降级）"""
    graph = StateGraph(AnalyzeState)

    graph.add_node("llm_analyze", node_llm_analyze)

    graph.set_entry_point("llm_analyze")
    graph.add_edge("llm_analyze", END)

    return graph.compile()
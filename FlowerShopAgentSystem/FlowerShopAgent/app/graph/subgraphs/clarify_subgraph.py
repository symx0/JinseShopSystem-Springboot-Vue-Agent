"""
子图2: 追问澄清 (Clarify Subgraph) — LLM 驱动版

策略：
  第0轮（首次）：ALWAYS 追问场景（不信任 LLM 的猜测，让用户确认）
  第1轮（场景已确认）：追问预算/风格偏好
  第2轮（预算/风格已知）：信息充足 → 进入推荐
  第3轮及以上：强制进入推荐

追问问题全部由 LLM 生成，无任何硬编码关键词匹配。
"""
import logging
from typing import TypedDict, Optional

from langgraph.graph import StateGraph, END

from app.config import settings

logger = logging.getLogger(__name__)


class ClarifyState(TypedDict, total=False):
    """追问澄清子图内部状态"""
    # 输入（来自 analyze_need）
    identified_scenario: Optional[str]
    scenario_confidence: float
    budget_min: Optional[float]
    budget_max: Optional[float]
    preferred_category: Optional[str]
    recipient: Optional[str]
    intent: str
    _clarify_round: int  # 当前轮次（0=首次，1=已问过场景，2=已问过预算）
    user_input: str

    # 中间产物：缺失信息标记
    missing_info: str  # "scenario" | "budget_or_category" | "none"

    # 输出
    need_more_info: bool
    follow_up_question: Optional[str]
    _route_to: str  # "recommend" | "respond"
    _clarify_round: int  # 更新后的轮次


def _llm_available() -> bool:
    return bool(settings.LLM_BASE_URL)


def node_check_info(state: ClarifyState) -> dict:
    """
    阶梯式信息检查。只做路由和轮次决策，不生成任何文字。

    轮次递增规则：
    - 第0轮：首次必问场景
    - 第1轮：场景已有 → 追问预算/风格
    - 第2轮及以上：信息足够 → 放行，否则最后追问一次
    """
    round_num = state.get("_clarify_round", 0)
    scenario = state.get("identified_scenario")
    budget_min = state.get("budget_min")
    budget_max = state.get("budget_max")
    has_budget = budget_min is not None or budget_max is not None
    category = state.get("preferred_category")
    intent = state.get("intent", "recommend")

    # 非推荐意图直接放行
    if intent in ("knowledge", "greeting", "confirm_order"):
        return {"need_more_info": False, "_route_to": "respond", "missing_info": "none"}

    # 第0轮：无论有没有场景，都先确认
    if round_num == 0:
        logger.info(f"[clarify] 第0轮：追问场景")
        return {
            "need_more_info": True,
            "_route_to": "respond",
            "missing_info": "scenario",
            "_clarify_round": 1,
        }

    # 第1轮：场景已有 → 追问预算/风格
    if round_num == 1:
        if not scenario:
            logger.info(f"[clarify] 第1轮：场景仍缺失，继续追问")
            return {
                "need_more_info": True, "_route_to": "respond",
                "missing_info": "scenario",
                "_clarify_round": 1,  # 保持同轮
            }
        if not has_budget and not category:
            logger.info(f"[clarify] 第1轮：追问预算/风格 → scenario={scenario}")
            return {
                "need_more_info": True, "_route_to": "respond",
                "missing_info": "budget_or_category",
                "_clarify_round": 2,
            }
        # 信息充足
        logger.info(f"[clarify] 第1轮：信息充足 → recommend")
        return {"need_more_info": False, "_route_to": "recommend", "missing_info": "none", "_clarify_round": 2}

    # 第2轮及以上：最后检查
    if not scenario:
        logger.info(f"[clarify] 第{round_num}轮：场景仍缺失，强制放行")
        return {"need_more_info": True, "_route_to": "respond", "missing_info": "scenario", "_clarify_round": round_num}

    logger.info(f"[clarify] 第{round_num}轮：信息充足 → recommend")
    return {"need_more_info": False, "_route_to": "recommend", "missing_info": "none"}


def node_generate_question(state: ClarifyState) -> dict:
    """
    LLM 根据已知信息 + 缺失信息类型，生成自然的追问问题。
    无任何硬编码文本模板。
    """
    missing = state.get("missing_info", "none")
    if missing == "none":
        return {}

    scenario = state.get("identified_scenario")
    recipient = state.get("recipient")
    budget_min = state.get("budget_min")
    budget_max = state.get("budget_max")
    category = state.get("preferred_category")
    user_input = state.get("user_input", "")

    # === LLM 路径 ===
    if _llm_available():
        try:
            from app.llm_factory import call_llm

            # 组装已知信息描述
            known_parts = []
            if scenario:
                known_parts.append(f"场景={scenario}")
            if recipient:
                known_parts.append(f"送礼对象={recipient}")
            if budget_min is not None or budget_max is not None:
                bmin = budget_min if budget_min is not None else 0
                bmax = budget_max if budget_max is not None else "不限"
                known_parts.append(f"预算=¥{bmin}~¥{bmax}")
            if category:
                known_parts.append(f"偏好={category}")
            known_text = "、".join(known_parts) if known_parts else "尚无明确信息"
            user_text = f"用户刚说：{user_input}" if user_input else "（首次对话）"

            if missing == "scenario":
                task = "请友好地询问用户送花的场景（例如：送恋人、母亲节、生日、自用装饰、乔迁等），如果已知送礼对象可以结合对象来问。"
            elif missing == "budget_or_category":
                task = "请友好地询问用户的预算范围或偏好风格（花束/盆栽/多肉/园林造景），可以给出价位参考但不要预设具体价格。"
            else:
                task = "请友好地了解用户还需要什么信息。"

            system_prompt = (
                "你是锦色花店的导购助手小锦，热情专业，像一位懂花的朋友。"
                "请直接以纯文本回复，不要使用任何函数调用或工具。"
                "你的任务是自然地向用户提问以收集信息。"
                "一次只问1-2个问题，保持亲切友好的语气。"
            )
            user_prompt = (
                f"已知用户信息：{known_text}\n"
                f"{user_text}\n\n"
                f"任务：{task}\n\n"
                f"请直接输出你的追问："
            )

            content = call_llm(
                system_prompt=system_prompt,
                user_message=user_prompt,
                temperature=0.8,
            )
            if content and len(content.strip()) > 5:
                return {"follow_up_question": content.strip()}
        except Exception as e:
            logger.warning(f"[clarify] LLM 问题生成失败: {e}")

    # === 降级：LLM 不可用时的最小化兜底（不含硬编码） ===
    fallback = _fallback_question(missing, scenario, recipient)
    return {"follow_up_question": fallback}


def _fallback_question(missing: str, scenario: Optional[str], recipient: Optional[str]) -> str:
    """
    降级兜底问题生成。不包含任何场景/对象关键词匹配，
    只根据信息缺失类型做极简描述。
    """
    if missing == "scenario":
        context = f"送给{recipient}的" if recipient else ""
        return f"请问您{context}是什么场合需要鲜花呢？比如节日、生日、表白，还是日常装饰？"
    elif missing == "budget_or_category":
        return (
            f"好的！请问您的预算大概是多少呢？或者有没有偏好的风格？\n"
            f"比如精致花束、盆栽绿植、多肉组合等等~"
        )
    return "还有什么我可以帮您了解的吗？"


def _route_after_check(state: ClarifyState) -> str:
    return state.get("_route_to", "recommend")


# ═══════════════════════════════════════════
#  构建子图
# ═══════════════════════════════════════════

def create_clarify_subgraph() -> StateGraph:
    """创建 LLM 驱动的追问澄清子图"""
    graph = StateGraph(ClarifyState)

    graph.add_node("check_info", node_check_info)
    graph.add_node("generate_question", node_generate_question)

    graph.set_entry_point("check_info")
    graph.add_conditional_edges(
        "check_info",
        _route_after_check,
        {"respond": "generate_question", "recommend": END},
    )
    graph.add_edge("generate_question", END)

    return graph.compile()
from typing import Annotated, Optional
from typing_extensions import TypedDict
from langgraph.graph.message import add_messages
from langchain_core.messages import BaseMessage

from app.models.schemas import RecommendationPlan


class ConversationState(TypedDict, total=False):
    """LangGraph对话状态

    核心状态流转：
    用户消息 -> 需求分析 -> 场景匹配 -> 商品推荐 -> 方案生成 -> 回复用户
    如果信息不足，则进入追问澄清节点，收集更多信息后重新分析
    """
    # 对话消息历史
    messages: Annotated[list[BaseMessage], add_messages]

    # 当前用户输入
    user_input: str

    # 需求分析结果
    identified_scenario: Optional[str]  # 识别到的场景ID
    scenario_confidence: float  # 场景识别置信度 0-1
    extracted_keywords: list[str]  # 提取的关键词

    # 用户偏好（通过对话逐步完善）
    budget_range: Optional[tuple[float, float]]  # 预算范围 (min, max)
    preferred_category: Optional[str]  # 偏好分类
    recipient: Optional[str]  # 送礼对象
    special_requirements: Optional[str]  # 特殊要求

    # 是否需要更多信息
    need_more_info: bool
    follow_up_question: Optional[str]  # 追问内容

    # 推荐方案
    plan: Optional[RecommendationPlan]

    # 当前步骤（用于路由）
    current_step: str  # "analyze" | "clarify" | "recommend" | "generate" | "respond"

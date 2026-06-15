"""LangGraph 对话状态定义 — 重构版v3

流程:
  START → classify → [路由]
    ├→ query_chitchat → respond → summarize → END (闲聊：模型自主决定是否调用工具)
    ├→ query_knowledge → respond → summarize → END (知识：模型自主调用RAG/商品工具)
    └→ extract_info → query_and_recommend → hallucination_check → respond → summarize → END (购物→推荐)
"""
from typing import Annotated, Optional
from typing_extensions import TypedDict
from langgraph.graph.message import add_messages
from langchain_core.messages import BaseMessage

from app.models.schemas import RecommendedOrder


class AgentState(TypedDict, total=False):
    """
    Agent 主状态

    所有路径共享此状态。不同路径只使用自己需要的字段。
    """
    # 对话消息历史（使用 add_messages reducer 实现增量追加）
    messages: Annotated[list[BaseMessage], add_messages]

    # 当前用户输入
    user_input: str

    # === 意图分类输出 ===
    # 用户意图类型
    user_type: str  # "chitchat" | "knowledge" | "shopping"

    # 意图置信度
    intent_confidence: float

    # 从用户输入中提取的关键词
    extracted_keywords: list[str]

    # === RAG 路径输出 ===
    # RAG 检索到的文档列表
    rag_documents: list[dict]

    # === 购物路径输出 ===
    # 需求分析
    identified_scenario: Optional[str]
    scenario_name: Optional[str]  # 场景中文名（如"母亲节"）
    scenario_id: Optional[str]    # 场景英文ID（如"mother_day"）
    scenario_confidence: float
    budget_min: Optional[float]  # 预算下限，默认0
    budget_max: Optional[float]  # 预算上限，由模型判断
    preferred_category: Optional[str]
    recipient: Optional[str]
    special_requirements: Optional[str]

    # 推荐订单
    order: Optional[RecommendedOrder]

    # === 购物路径中间数据 ===
    # 数据库查询原始结果（MCP 返回的 JSON）
    _raw_products: Optional[str]
    _raw_activities: Optional[str]
    _raw_knowledge: Optional[str]

    # 幻觉检查结果
    _hallucination_flags: list[dict]
    _order_valid: bool
    _recommend_failed_rounds: int
    _recommend_error: Optional[str]  # 推荐失败原因（如"花店暂无您需要的商品"）

    # 会话ID（关联数据库会话主键）
    session_id: Optional[str]

    # 用户ID（关联数据库用户）
    user_id: Optional[str]

    # 澄清轮次计数（shopping 路径用，确保至少追问一轮）
    _clarify_round: int

    # 当前步骤标识
    current_step: str  # classify | rag | clarify | research | verify | present | confirm | end

    # 会话总结（LLM对过往问答的总结，用于意图分类时理解上下文）
    thinking_chain: str

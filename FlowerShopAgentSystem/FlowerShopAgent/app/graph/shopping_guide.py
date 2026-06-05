import logging
from langgraph.graph import StateGraph, END

from app.graph.state import ConversationState
from app.graph.nodes import analyze_need, clarify_need, recommend_products, generate_response

logger = logging.getLogger(__name__)


def _route_after_clarify(state: ConversationState) -> str:
    """澄清后的路由：需要更多信息则直接回复，否则进入推荐"""
    if state.get("need_more_info", False):
        return "respond"
    return "recommend"


def _route_after_analyze(state: ConversationState) -> str:
    """分析后的路由：进入澄清节点"""
    return "clarify"


def build_shopping_guide_graph() -> StateGraph:
    """构建导购Agent的LangGraph工作流

    工作流：
    analyze(需求分析) -> clarify(追问澄清) -> recommend(商品推荐) -> generate(响应生成)
                                    |
                                    +-> respond(直接回复追问) -> END
    """
    graph = StateGraph(ConversationState)

    # 添加节点
    graph.add_node("analyze", analyze_need)
    graph.add_node("clarify", clarify_need)
    graph.add_node("recommend", recommend_products)
    graph.add_node("generate", generate_response)

    # 设置入口
    graph.set_entry_point("analyze")

    # 添加边
    graph.add_edge("analyze", "clarify")
    graph.add_conditional_edges(
        "clarify",
        _route_after_clarify,
        {
            "respond": "generate",
            "recommend": "recommend",
        },
    )
    graph.add_edge("recommend", "generate")
    graph.add_edge("generate", END)

    return graph


def create_shopping_guide_app():
    """编译并返回导购Agent应用"""
    graph = build_shopping_guide_graph()
    app = graph.compile()
    logger.info("导购Agent工作流编译完成")
    return app

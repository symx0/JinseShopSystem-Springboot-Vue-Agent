"""
LangGraph 子图模块

每个子图封装一个独立的业务领域，由主图（main_graph）进行编排调度。

子图架构（重构版）:
├── classify_intent_subgraph — 意图分类: chitchat | knowledge | shopping
├── rag_subgraph             — RAG 检索 + 知识回复（chitchat/knowledge 路径）
├── clarify_subgraph         — 需求澄清（shopping 路径专用）
├── recommend_subgraph       — 商品推荐 / Researcher 模式（shopping 路径）
└── respond_subgraph         — 统一回复生成（所有路径）

主图编排（重构版）:
  START → classify → [路由]
                       ├→ rag → respond → END (chitchat/knowledge)
                       └→ clarify → [路由]
                                      ├→ respond → END (追问)
                                      └→ recommend → hallucination → respond → END
"""

from app.graph.subgraphs.classify_intent_subgraph import create_classify_intent_subgraph
from app.graph.subgraphs.rag_subgraph import create_rag_subgraph
from app.graph.subgraphs.clarify_subgraph import create_clarify_subgraph
from app.graph.subgraphs.recommend_subgraph import create_recommend_subgraph
from app.graph.subgraphs.respond_subgraph import create_respond_subgraph
from app.graph.subgraphs.analyze_subgraph import create_analyze_subgraph

__all__ = [
    "create_classify_intent_subgraph",
    "create_rag_subgraph",
    "create_clarify_subgraph",
    "create_recommend_subgraph",
    "create_respond_subgraph",
    "create_analyze_subgraph",
]
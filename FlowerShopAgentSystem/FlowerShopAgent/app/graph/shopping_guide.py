"""
导购Agent图入口（兼容旧版接口）

图架构 (重构版):
  - main_graph.py: 主编排图
  - subgraphs/classify_intent_subgraph.py: 意图分类子图
  - subgraphs/rag_subgraph.py: RAG 知识检索子图
  - subgraphs/clarify_subgraph.py: 需求澄清子图
  - subgraphs/recommend_subgraph.py: Researcher 商品推荐子图
  - subgraphs/respond_subgraph.py: 统一回复生成子图
"""
from app.graph.main_graph import create_shopping_guide_app, build_main_graph

__all__ = ["create_shopping_guide_app", "build_main_graph"]

"""
LangGraph 导购Agent 图模块 (重构版)

图架构:
  main_graph (编排层)
    ├── classify_intent_subgraph — 意图分类: chitchat | knowledge | shopping
    ├── rag_subgraph             — RAG 检索 + 知识回复
    ├── clarify_subgraph         — 需求澄清（shopping 路径循环确认）
    ├── recommend_subgraph       — Researcher 模式: MCP MySQL 查询 + 订单构建
    └── respond_subgraph         — 统一回复生成 + 润色

使用方式:
  from app.graph.main_graph import create_shopping_guide_app
  app = create_shopping_guide_app()
  result = app.ainvoke(state)
"""

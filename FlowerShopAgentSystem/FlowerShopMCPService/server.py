"""
锦色花店 MCP Server
提供工具（MySQL查询）和资源（花卉常识）供Agent调用

启动方式:
  python server.py          # stdio 模式（默认，供Agent进程调用）
  python server.py --sse    # SSE 模式（HTTP服务，供调试用）
"""
import argparse
import json
import logging
import os
import sys
from pathlib import Path

import yaml
from mcp.server import FastMCP

from tools.db_tools import (
    init_db_pool,
    search_flowers,
    get_flower_by_id,
    get_ongoing_activities,
    get_activity_flowers,
    get_all_categories,
    get_all_flowers_with_promos,
    get_flower_by_scenario_keywords,
)

# 配置日志
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(name)s] %(levelname)s: %(message)s")
logger = logging.getLogger("FlowerShopMCPServer")

# 加载配置
CONFIG_PATH = Path(__file__).parent / "config.yaml"
with open(CONFIG_PATH, "r", encoding="utf-8") as f:
    config = yaml.safe_load(f)

# 初始化数据库连接池
init_db_pool(config)

# 创建 MCP Server
mcp = FastMCP(
    name=config["server"]["name"],
    # version=config["server"]["version"],
)

# ═══════════════════════════════════════════
#  工具 (Tools)
# ═══════════════════════════════════════════

@mcp.tool()
def tool_search_flowers(
    keyword: str = None,
    category_id: int = None,
    min_price: float = None,
    max_price: float = None,
    limit: int = 20,
) -> str:
    """
    搜索鲜花商品。
    参数:
    - keyword: 搜索关键词（匹配名称和描述）
    - category_id: 分类ID
    - min_price: 最低价格
    - max_price: 最高价格
    - limit: 返回数量上限（默认20）
    """
    try:
        results = search_flowers(
            keyword=keyword,
            category_id=category_id,
            min_price=min_price,
            max_price=max_price,
            limit=limit,
        )
        # 转换数据类型，确保JSON可序列化
        for r in results:
            for k, v in r.items():
                if hasattr(v, "isoformat"):
                    r[k] = v.isoformat()
        return json.dumps(results, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"搜索鲜花失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_get_flower_detail(flower_id: int) -> str:
    """
    查询鲜花详情，包含活动促销信息。
    参数:
    - flower_id: 鲜花ID
    """
    try:
        result = get_flower_by_id(flower_id)
        if result:
            for k, v in result.items():
                if hasattr(v, "isoformat"):
                    result[k] = v.isoformat()
        return json.dumps(result, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"查询鲜花详情失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_get_ongoing_activities() -> str:
    """查询当前进行中的所有活动"""
    try:
        results = get_ongoing_activities()
        for r in results:
            for k, v in r.items():
                if hasattr(v, "isoformat"):
                    r[k] = v.isoformat()
        return json.dumps(results, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"查询活动失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_get_activity_flowers(activity_id: int) -> str:
    """
    查询活动下的促销鲜花列表。
    参数:
    - activity_id: 活动ID
    """
    try:
        results = get_activity_flowers(activity_id)
        for r in results:
            for k, v in r.items():
                if hasattr(v, "isoformat"):
                    r[k] = v.isoformat()
        return json.dumps(results, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"查询活动鲜花失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_get_categories() -> str:
    """查询所有启用的鲜花分类"""
    try:
        results = get_all_categories()
        for r in results:
            for k, v in r.items():
                if hasattr(v, "isoformat"):
                    r[k] = v.isoformat()
        return json.dumps(results, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"查询分类失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_get_all_flowers_with_promos() -> str:
    """获取所有在售鲜花，包含活动促销信息（用于构建推荐订单）"""
    try:
        results = get_all_flowers_with_promos()
        for r in results:
            for k, v in r.items():
                if hasattr(v, "isoformat"):
                    r[k] = v.isoformat()
        return json.dumps(results, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"查询全部鲜花失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_search_by_scenario(keywords: str) -> str:
    """
    根据场景关键词推荐鲜花。输入逗号分隔的关键词列表。
    例如: "表白,浪漫,玫瑰花束"
    """
    try:
        kw_list = [kw.strip() for kw in keywords.split(",") if kw.strip()]
        results = get_flower_by_scenario_keywords(kw_list)
        for r in results:
            for k, v in r.items():
                if hasattr(v, "isoformat"):
                    r[k] = v.isoformat()
        return json.dumps(results, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"场景搜索失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


# ═══════════════════════════════════════════
#  RAG 知识库工具
# ═══════════════════════════════════════════

from tools.rag_tools import init_rag, search_knowledge, get_knowledge_categories, get_document_content, configure_embedding, get_status

# 初始化RAG知识库（默认 TF-IDF，后续可通过工具切换）
init_rag()


@mcp.tool()
def tool_rag_search(query: str, top_k: int = 5, category: str = None) -> str:
    """
    RAG知识库检索：根据查询语句检索花卉相关知识。
    参数:
    - query: 查询文本（如"玫瑰花怎么养护"、"表白送什么花"）
    - top_k: 返回结果数量（默认5）
    - category: 限定分类（可选：鲜花养护/花语含义/场景选花/品种介绍/行业知识）
    """
    try:
        results = search_knowledge(query, top_k=top_k, category_filter=category)
        return json.dumps(results, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"RAG检索失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_rag_categories() -> str:
    """获取RAG知识库的所有分类及文档列表"""
    try:
        categories = get_knowledge_categories()
        return json.dumps(categories, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"获取分类失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_rag_get_document(category: str, filename: str) -> str:
    """
    获取指定文档的完整内容。
    参数:
    - category: 文档分类（如"鲜花养护"）
    - filename: 文件名（如"鲜切花养护"）
    """
    try:
        content = get_document_content(category, filename)
        if not content:
            return json.dumps({"error": "文档不存在"}, ensure_ascii=False)
        return content[:5000]  # 截断过长文档
    except Exception as e:
        logger.error(f"获取文档失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_rag_configure_embedding(base_url: str = "", api_key: str = "", model: str = "") -> str:
    """
    配置 RAG 嵌入模型，配置后自动构建双路融合检索索引（BM25+Similarity）。
    参数:
    - base_url: 嵌入模型 API 地址（需包含 /v1 后缀或自动补全）
      - Ollama 示例: http://localhost:11434/v1
      - OpenAI 示例: https://api.openai.com/v1
    - api_key: API Key（Ollama 可留空）
    - model: 嵌入模型名称
      - Ollama 可选: nomic-embed-text, mxbai-embed-large, qwen3-embedding:8b 等
      - OpenAI 可选: text-embedding-3-small, text-embedding-3-large 等
    """
    try:
        result = configure_embedding(base_url=base_url, api_key=api_key, model=model)
        return json.dumps(result, ensure_ascii=False)
    except Exception as e:
        logger.error(f"配置嵌入模型失败: {e}")
        return json.dumps({"success": False, "error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_rag_embedding_status() -> str:
    """获取当前 RAG 嵌入模型的状态信息（模式、模型、索引记录数等）"""
    try:
        status = get_status()
        return json.dumps(status, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"获取嵌入状态失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.resource("rag://knowledge/categories")
def get_rag_categories_resource() -> str:
    """RAG知识库分类概览"""
    categories = get_knowledge_categories()
    return json.dumps(categories, ensure_ascii=False, indent=2)


# ═══════════════════════════════════════════
#  知识库管理工具（上传/列表/删除）
# ═══════════════════════════════════════════

from tools.knowledge_manager import (
    init_knowledge_dir,
    list_categories,
    upload_document,
    delete_document,
)

init_knowledge_dir()


@mcp.tool()
def tool_knowledge_list() -> str:
    """
    列出知识库所有分类及文档。
    返回每个分类下的文档文件名、大小、修改时间。
    """
    try:
        categories = list_categories()
        return json.dumps(categories, ensure_ascii=False, indent=2)
    except Exception as e:
        logger.error(f"列出知识库失败: {e}")
        return json.dumps({"error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_knowledge_upload(category: str, filename: str, content: str) -> str:
    """
    上传或更新知识库文档。
    参数:
    - category: 分类名（鲜花养护/花语含义/场景选花/品种介绍/行业知识）
    - filename: 文件名（如"绣球花养护.txt"）
    - content: 文档内容（纯文本）
    上传后会自动重建RAG索引。
    """
    try:
        result = upload_document(category, filename, content)
        return json.dumps(result, ensure_ascii=False)
    except Exception as e:
        logger.error(f"上传文档失败: {e}")
        return json.dumps({"success": False, "error": str(e)}, ensure_ascii=False)


@mcp.tool()
def tool_knowledge_delete(category: str, filename: str) -> str:
    """
    删除知识库文档。
    参数:
    - category: 分类名
    - filename: 文件名
    删除后会自动重建RAG索引。
    """
    try:
        result = delete_document(category, filename)
        return json.dumps(result, ensure_ascii=False)
    except Exception as e:
        logger.error(f"删除文档失败: {e}")
        return json.dumps({"success": False, "error": str(e)}, ensure_ascii=False)


# ═══════════════════════════════════════════
#  启动入口
# ═══════════════════════════════════════════

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="锦色花店 MCP Server")
    parser.add_argument("--sse", action="store_true", help="以SSE模式启动（MCP over HTTP），默认为stdio模式")
    parser.add_argument("--host", type=str, default="0.0.0.0")
    parser.add_argument("--port", type=int, default=8001)
    args = parser.parse_args()

    if args.sse:
        logger.info(f"MCP Server SSE模式启动: http://{args.host}:{args.port}")
        mcp.run(transport="sse", host=args.host, port=args.port)
    else:
        logger.info("MCP Server stdio模式启动")
        mcp.run(transport="stdio")

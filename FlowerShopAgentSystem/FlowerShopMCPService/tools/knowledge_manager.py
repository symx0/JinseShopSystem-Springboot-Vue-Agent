"""
知识库文档管理模块

管理 MCP Server knowledge_docs/ 目录下的知识文档。
支持文档的上传、列表、删除功能，删除/上传后自动重建 RAG 索引。
"""
import logging
import shutil
from pathlib import Path
from typing import Optional

logger = logging.getLogger(__name__)

# 知识库根目录
_KNOWLEDGE_DIR = Path(__file__).parent.parent / "knowledge_docs"

# 合法分类名
VALID_CATEGORIES = ["鲜花养护", "花语含义", "场景选花", "品种介绍", "行业知识"]


def init_knowledge_dir():
    """确保知识库目录结构存在"""
    logger.info("初始化知识库目录结构")
    _KNOWLEDGE_DIR.mkdir(parents=True, exist_ok=True)
    for cat in VALID_CATEGORIES:
        (_KNOWLEDGE_DIR / cat).mkdir(exist_ok=True)


def list_categories() -> list[dict]:
    """列出所有分类及文档列表"""
    logger.info("列出所有分类及文档列表")
    init_knowledge_dir()
    categories = []
    for cat in VALID_CATEGORIES:
        cat_dir = _KNOWLEDGE_DIR / cat
        if not cat_dir.exists():
            continue
        files = []
        for f in cat_dir.rglob("*"):
            if f.is_file() and f.suffix.lower() in (".txt", ".md"):
                stat = f.stat()
                files.append({
                    "filename": f.name,
                    "relative_path": str(f.relative_to(_KNOWLEDGE_DIR)),
                    "size_bytes": stat.st_size,
                    "modified_at": stat.st_mtime,
                })
        categories.append({
            "category": cat,
            "path": str(cat_dir.relative_to(_KNOWLEDGE_DIR)),
            "file_count": len(files),
            "files": files,
        })
    return categories


def upload_document(category: str, filename: str, content: str) -> dict:
    """
    上传/更新文档到指定分类

    Args:
        category: 分类名（鲜花养护/花语含义/场景选花/品种介绍/行业知识）
        filename: 文件名（如 "鲜切花养护.txt"）
        content: 文档内容（文本）

    Returns:
        上传结果
    """
    logger.info(f"上传文档 {category}/{filename}")
    if category not in VALID_CATEGORIES:
        return {"success": False, "error": f"无效分类: {category}。可选: {', '.join(VALID_CATEGORIES)}"}

    # 安全检查：文件名不能包含路径穿越
    if ".." in filename or "/" in filename or "\\" in filename:
        return {"success": False, "error": "文件名包含非法字符"}

    init_knowledge_dir()
    cat_dir = _KNOWLEDGE_DIR / category

    # 添加 .txt 后缀（如果没有的话）
    if not filename.endswith((".txt", ".md")):
        filename = filename + ".txt"

    file_path = cat_dir / filename

    try:
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)

        logger.info(f"文档已保存: {category}/{filename} ({len(content)} 字符)")

        # 重建 RAG 索引
        _rebuild_rag_index()

        return {
            "success": True,
            "category": category,
            "filename": filename,
            "path": str(file_path.relative_to(_KNOWLEDGE_DIR)),
            "size": len(content),
        }
    except Exception as e:
        logger.error(f"文档上传失败: {e}")
        return {"success": False, "error": str(e)}


def delete_document(category: str, filename: str) -> dict:
    """
    删除指定文档

    Args:
        category: 分类名
        filename: 文件名

    Returns:
        删除结果
    """
    logger.info(f"删除文档 {category}/{filename}")
    if category not in VALID_CATEGORIES:
        return {"success": False, "error": f"无效分类: {category}"}

    if ".." in filename or "/" in filename or "\\" in filename:
        return {"success": False, "error": "文件名包含非法字符"}

    file_path = _KNOWLEDGE_DIR / category / filename

    if not file_path.exists():
        return {"success": False, "error": f"文件不存在: {category}/{filename}"}

    try:
        file_path.unlink()
        logger.info(f"文档已删除: {category}/{filename}")

        # 重建 RAG 索引
        _rebuild_rag_index()

        return {"success": True, "category": category, "filename": filename}
    except Exception as e:
        logger.error(f"文档删除失败: {e}")
        return {"success": False, "error": str(e)}


def get_document_content(category: str, filename: str) -> Optional[str]:
    """
    获取指定文档的完整内容

    Args:
        category: 分类名
        filename: 文件名

    Returns:
        文档内容，或 None
    """
    logger.info(f"读取文档 {category}/{filename}")
    if category not in VALID_CATEGORIES:
        return None

    file_path = _KNOWLEDGE_DIR / category / filename
    if not file_path.exists():
        return None

    try:
        with open(file_path, "r", encoding="utf-8") as f:
            return f.read()
    except Exception as e:
        logger.error(f"读取文档失败: {e}")
        return None


def _rebuild_rag_index():
    """重建 RAG 索引（重新加载文档 + 重建 BM25/Chroma）"""
    logger.info("重建 RAG 索引")
    try:
        from tools.rag_tools import load_knowledge_docs, _build_retrievers
        load_knowledge_docs()
        from tools.rag_tools import _embedding_configured
        if _embedding_configured:
            _build_retrievers()
            logger.info("RAG索引已重建")
        else:
            logger.info("嵌入模型未配置，仅重新加载文档，索引待配置后重建")
    except Exception as e:
        logger.warning(f"RAG索引重建失败: {e}")

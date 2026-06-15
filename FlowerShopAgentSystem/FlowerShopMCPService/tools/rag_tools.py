"""
MCP Server RAG 知识库检索工具 — LangChain EnsembleRetriever 版

检索策略: BM25 + Chroma向量(similarity) 双路融合
  - BM25 关键词匹配 (权重 0.4)  — 使用 langchain BM25Retriever
  - Chroma 向量余弦相似度 (权重 0.6) — 使用 langchain EnsembleRetriever

嵌入模型: 统一使用 langchain_openai.OpenAIEmbeddings
  - Ollama: base_url=http://localhost:11434/v1, api_key 可留空
  - OpenAI: base_url=https://api.openai.com/v1, api_key 必填
  两者接口一致，无需区分模式。

依赖: chromadb, langchain, langchain-community, langchain-openai, langchain-classic, rank_bm25
"""
import json
import logging
import re
from pathlib import Path
from typing import Optional

import yaml

logger = logging.getLogger(__name__)

KNOWLEDGE_DOCS_DIR = Path(__file__).parent.parent / "knowledge_docs"
CHROMA_DIR = Path(__file__).parent.parent / "chroma_db"
CONFIG_PATH = Path(__file__).parent.parent / "config.yaml"

# ═══════════════════════════════════════════
#  全局状态
# ═══════════════════════════════════════════

_documents: list[dict] = []           # 原始文档列表
_langchain_docs: list = []            # LangChain Document 列表
_bm25_retriever = None                # BM25Retriever (langchain_community)
_chroma_vectorstore = None            # LangChain Chroma VectorStore
_ensemble_retriever = None            # EnsembleRetriever (langchain_classic)
_embeddings = None                    # LangChain Embeddings 对象

_embedding_configured: bool = False
_embedding_config: dict = {}

_RAG_TOP_K: int = 5                    # 默认返回条数


# ═══════════════════════════════════════════
#  文档加载 + 分块
# ═══════════════════════════════════════════

def load_knowledge_docs(base_dir: Path = None) -> list[dict]:
    """加载知识库目录下所有 .txt/.md 文件，分块并转为 LangChain Document"""
    logger.info("----------加载知识库文档----------")
    global _documents, _langchain_docs
    if base_dir is None:
        base_dir = KNOWLEDGE_DOCS_DIR
    _documents = []
    _langchain_docs = []
    if not base_dir.exists():
        logger.warning(f"[RAG] 知识库目录不存在: {base_dir}")
        return []
    for txt_file in base_dir.rglob("*"):
        if txt_file.suffix.lower() not in (".txt", ".md"):
            continue
        try:
            try:
                with open(txt_file, "r", encoding="utf-8") as f:
                    content = f.read()
            except UnicodeDecodeError:
                with open(txt_file, "r", encoding="gbk") as f:
                    content = f.read()
        except Exception as e:
            logger.warning(f"[RAG] 读取失败 {txt_file}: {e}")
            continue
        rel = txt_file.relative_to(base_dir)
        category = rel.parts[0] if len(rel.parts) > 1 else "未分类"
        doc = {"path": str(txt_file), "category": category, "filename": txt_file.stem, "content": content, "chunks": []}
        for para in re.split(r'\n\s*\n', content):
            para = para.strip()
            if len(para) < 10:
                continue
            doc["chunks"].extend([para[i:i + 500] for i in range(0, len(para), 500)])
        _documents.append(doc)

        # 构建 LangChain Document
        try:
            from langchain_core.documents import Document
            for ci, chunk_text in enumerate(doc["chunks"]):
                _langchain_docs.append(Document(
                    page_content=chunk_text,
                    metadata={"category": category, "filename": txt_file.stem, "chunk_idx": ci},
                ))
        except ImportError:
            pass

    logger.info(f"[RAG] 文档加载: {len(_documents)} 文档, {len(_langchain_docs)} LangChain块")
    return _documents


# ═══════════════════════════════════════════
#  构建检索器（BM25 + Chroma Similarity 双路融合）
# ═══════════════════════════════════════════

RETRIEVER_WEIGHTS = {"bm25": 0.4, "similarity": 0.6}


def _build_retrievers():
    """
    构建 BM25 + Chroma Similarity 双路检索器，
    使用 langchain_classic.retrievers.EnsembleRetriever 加权融合。
    """
    logger.info("----------构建双路融合检索器----------")
    global _bm25_retriever, _chroma_vectorstore, _ensemble_retriever

    _ensemble_retriever = None

    try:
        from langchain_community.retrievers import BM25Retriever
        from langchain_classic.retrievers import EnsembleRetriever
    except ImportError as e:
        logger.error(f"[RAG] langchain 依赖缺失: {e}，请 pip install langchain-community langchain-classic")
        return

    if not _langchain_docs:
        logger.warning("[RAG] 无文档，跳过检索器构建")
        return

    if _embeddings is None:
        logger.warning("[RAG] 嵌入模型未配置，跳过向量检索器构建")
        return

    retrievers = []
    weights = []

    # 1. BM25 Retriever
    try:
        logger.info("[RAG] 构建 BM25 Retriever（langchain_community）")
        _bm25_retriever = BM25Retriever.from_documents(_langchain_docs, k=_RAG_TOP_K)
        retrievers.append(_bm25_retriever)
        weights.append(RETRIEVER_WEIGHTS["bm25"])
        logger.info(f"[RAG] BM25 Retriever: k={_RAG_TOP_K}")
    except Exception as e:
        logger.error(f"[RAG] BM25 Retriever 构建失败: {e}")
        _bm25_retriever = None

    # 2. Chroma VectorStore + Similarity Retriever
    try:
        from langchain_community.vectorstores import Chroma as LangChainChroma
        import chromadb

        CHROMA_DIR.mkdir(parents=True, exist_ok=True)
        client = chromadb.PersistentClient(path=str(CHROMA_DIR))
        try:
            client.delete_collection("flower_knowledge")
            logger.info("[RAG] 已清除旧 Chroma 集合")
        except Exception:
            pass

        _chroma_vectorstore = LangChainChroma.from_documents(
            documents=_langchain_docs,
            embedding=_embeddings,
            collection_name="flower_knowledge",
            persist_directory=str(CHROMA_DIR),
        )
        chroma_count = _chroma_vectorstore._collection.count()
        logger.info(f"[RAG] Chroma 索引: {chroma_count} 条向量")

        retriever_similarity = _chroma_vectorstore.as_retriever(
            search_type="similarity",
            search_kwargs={"k": _RAG_TOP_K},
        )
        retrievers.append(retriever_similarity)
        weights.append(RETRIEVER_WEIGHTS["similarity"])
        logger.info(f"[RAG] Similarity Retriever: k={_RAG_TOP_K}")
    except Exception as e:
        logger.error(f"[RAG] Chroma VectorStore 构建失败: {e}")
        import traceback
        traceback.print_exc()
        _chroma_vectorstore = None

    # 3. Ensemble Retriever (langchain_classic)
    if len(retrievers) >= 2:
        _ensemble_retriever = EnsembleRetriever(retrievers=retrievers, weights=weights)
        logger.info(f"[RAG] EnsembleRetriever: {len(retrievers)}路, weights={weights}")
    elif len(retrievers) == 1:
        _ensemble_retriever = retrievers[0]
        logger.info(f"[RAG] 单路检索器: weights={weights}")
    else:
        _ensemble_retriever = None
        logger.warning("[RAG] 无可用检索器")


# ═══════════════════════════════════════════
#  统一检索入口
# ═══════════════════════════════════════════

def search_knowledge(query: str, top_k: int = 5, category_filter: str = None) -> list[dict]:
    """
    RAG 检索入口，使用 EnsembleRetriever (BM25 + Chroma Similarity) 双路融合检索。
    """
    logger.info("----------双路融合检索----------")
    if not _documents:
        load_knowledge_docs()

    if _ensemble_retriever is None:
        logger.warning("[RAG] 检索器未构建，请在管理端配置嵌入模型后重试")
        return []

    try:
        lc_docs = _ensemble_retriever.invoke(query)

        results = []
        seen = set()
        for doc in lc_docs:
            meta = doc.metadata if hasattr(doc, "metadata") else {}
            cat = meta.get("category", "未分类")
            fn = meta.get("filename", "")

            if category_filter and cat != category_filter:
                continue

            key = (cat, fn)
            if key in seen:
                continue
            seen.add(key)

            results.append({
                "category": cat,
                "filename": fn,
                "text": doc.page_content[:800] if hasattr(doc, "page_content") else str(doc)[:800],
                "score": 1.0,
                "source": "ensemble",
            })
            if len(results) >= top_k:
                break

        logger.info(f"[RAG] Ensemble检索: query='{query[:30]}...', top_k={top_k}, 命中={len(results)}")
        return results
    except Exception as e:
        logger.error(f"[RAG] 检索失败: {e}")
        return []


# ═══════════════════════════════════════════
#  嵌入模型配置
# ═══════════════════════════════════════════

def configure_embedding(base_url: str = "", api_key: str = "", model: str = "") -> dict:
    """配置嵌入模型并自动构建向量存储库。自动检测 Ollama/OpenAI SDK，配置后构建双路检索器。"""
    logger.info("----------嵌入模型配置----------")
    global _embedding_configured, _embedding_config, _embeddings

    try:
        # 自动检测 Ollama vs OpenAI
        is_ollama = "11434" in base_url or "ollama" in base_url.lower()

        if is_ollama:
            # Ollama: 使用 langchain_community OllamaEmbeddings（原生 /api/embed 接口）
            from langchain_community.embeddings import OllamaEmbeddings

            ollama_base = base_url.rstrip("/")
            if ollama_base.endswith("/v1"):
                ollama_base = ollama_base[:-3]  # OllamaEmbeddings 不需要 /v1

            _embeddings = OllamaEmbeddings(
                model=model,
                base_url=ollama_base,
            )
            logger.info(f"[RAG] 使用 OllamaEmbeddings: {model}@{ollama_base}")
        else:
            # OpenAI 兼容: 使用 langchain_openai OpenAIEmbeddings
            from langchain_openai import OpenAIEmbeddings

            normalized_url = base_url.rstrip("/")
            if not normalized_url.endswith("/v1"):
                normalized_url += "/v1"

            _embeddings = OpenAIEmbeddings(
                model=model,
                openai_api_key=api_key or "not-needed",
                openai_api_base=normalized_url,
            )
            logger.info(f"[RAG] 使用 OpenAIEmbeddings: {model}@{normalized_url}")

        # 测试嵌入是否可用
        logger.info(f"[RAG] 测试嵌入模型...")
        test_result = _embeddings.embed_query("测试")
        if not test_result or len(test_result) == 0:
            raise ValueError("嵌入模型返回空结果")

        _embedding_configured = True
        _embedding_config = {"base_url": base_url, "api_key": api_key, "model": model}

        # 持久化嵌入配置到 config.yaml
        _save_embedding_config(base_url, api_key, model)

        # 确保文档已加载
        if not _documents:
            load_knowledge_docs()

        # 自动构建向量存储库和检索器
        logger.info(f"[RAG] 开始构建向量存储库（{_langchain_docs.__len__()} 个文档块）...")
        _build_retrievers()

        # 检查构建结果
        if _ensemble_retriever is None:
            _embedding_configured = False
            return {"success": False, "error": "检索器（含BM25和Chroma）全部构建失败，请检查日志"}

        chroma_count = 0
        chroma_failed = False
        if _chroma_vectorstore is not None:
            try:
                chroma_count = _chroma_vectorstore._collection.count()
            except Exception:
                pass
        else:
            chroma_failed = True
            logger.warning(f"[RAG] Chroma 向量检索器构建失败，仅使用 BM25 关键词检索。"
                           f"建议换用小模型（如 nomic-embed-text 或 bge-m3）。")

        status_msg = (f"已激活 {model} 双路融合检索（BM25+Similarity），向量库 {chroma_count} 条"
                      if not chroma_failed else
                      f"已激活 {model} BM25 关键词检索（Chroma 向量库构建失败，"
                      f"可能是模型过大导致 Ollama 502，建议换用 nomic-embed-text 或 bge-m3）")

        logger.info(f"[RAG] 嵌入模型配置完成: {model}@{base_url}, Chroma向量={chroma_count}条")
        return {
            "success": True,
            "model": model,
            "chroma_records": chroma_count,
            "langchain_docs": len(_langchain_docs),
            "chroma_failed": chroma_failed,
            "message": status_msg,
        }

    except ImportError:
        logger.error("[RAG] langchain-openai 未安装，pip install langchain-openai")
        return {"success": False, "error": "langchain-openai 未安装，请执行: pip install langchain-openai"}
    except Exception as e:
        logger.error(f"[RAG] 嵌入模型配置失败: {e}")
        _embedding_configured = False
        _embeddings = None
        return {"success": False, "error": str(e)}


def set_top_k(top_k: int):
    """设置检索返回条数"""
    global _RAG_TOP_K
    _RAG_TOP_K = max(1, min(top_k, 20))


def get_top_k() -> int:
    return _RAG_TOP_K


# ═══════════════════════════════════════════
#  辅助接口
# ═══════════════════════════════════════════

def get_knowledge_categories() -> list[dict]:
    if not _documents:
        load_knowledge_docs()
    cats = {}
    for doc in _documents:
        cat = doc["category"]
        if cat not in cats:
            cats[cat] = []
        cats[cat].append({"filename": doc["filename"], "path": doc["path"],
                           "chunks_count": len(doc["chunks"]), "size": len(doc["content"])})
    return [{"category": cat, "documents": docs} for cat, docs in cats.items()]


def get_document_content(category: str, filename: str) -> str:
    for doc in _documents:
        if doc["category"] == category and doc["filename"] == filename:
            return doc["content"]
    return ""


def get_status() -> dict:
    chroma_count = 0
    if _chroma_vectorstore is not None:
        try:
            chroma_count = _chroma_vectorstore._collection.count()
        except Exception:
            pass

    return {
        "configured": _embedding_configured,
        "config": {"base_url": _embedding_config.get("base_url", ""), "model": _embedding_config.get("model", "")},
        "documents_count": len(_documents),
        "top_k": _RAG_TOP_K,
        "langchain_docs": len(_langchain_docs),
        "chroma_records": chroma_count,
        "retriever": "ensemble" if _ensemble_retriever is not None else "none",
    }


def _save_embedding_config(base_url: str, api_key: str, model: str):
    """将嵌入模型配置持久化到 config.yaml"""
    try:
        existing = {}
        if CONFIG_PATH.exists():
            with open(CONFIG_PATH, "r", encoding="utf-8") as f:
                existing = yaml.safe_load(f) or {}

        existing["embedding"] = {"base_url": base_url, "api_key": api_key, "model": model}

        with open(CONFIG_PATH, "w", encoding="utf-8") as f:
            yaml.dump(existing, f, default_flow_style=False, allow_unicode=True)

        logger.info(f"[RAG] 嵌入配置已持久化到 config.yaml: model={model}")
    except Exception as e:
        logger.warning(f"[RAG] 嵌入配置持久化失败: {e}")


def _load_embedding_config() -> dict:
    """从 config.yaml 读取嵌入模型配置"""
    try:
        if CONFIG_PATH.exists():
            with open(CONFIG_PATH, "r", encoding="utf-8") as f:
                data = yaml.safe_load(f) or {}
            emb = data.get("embedding", {})
            if emb.get("model"):
                return emb
    except Exception as e:
        logger.warning(f"[RAG] 读取嵌入配置失败: {e}")
    return {}


def init_rag():
    """初始化：加载文档 + 自动恢复已保存的嵌入配置"""
    load_knowledge_docs()

    # 尝试从 config.yaml 恢复嵌入配置
    saved_config = _load_embedding_config()
    if saved_config.get("model"):
        logger.info(f"[RAG] 检测到已保存的嵌入配置: model={saved_config['model']}, 自动恢复中...")
        result = configure_embedding(
            base_url=saved_config.get("base_url", ""),
            api_key=saved_config.get("api_key", ""),
            model=saved_config["model"],
        )
        if result.get("success"):
            logger.info(f"[RAG] 嵌入模型自动恢复成功: {saved_config['model']}")
        else:
            logger.warning(f"[RAG] 嵌入模型自动恢复失败: {result.get('error', '未知错误')}")
    else:
        logger.info(f"[RAG] 初始化完成: 嵌入模型未配置，等待管理端配置。文档数={len(_documents)}")

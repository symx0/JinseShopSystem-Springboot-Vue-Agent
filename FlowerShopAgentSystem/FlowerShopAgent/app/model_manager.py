"""
模型管理器 — 支持大语言模型和嵌入模型的运行时热切换 + 磁盘持久化

配置来源: config.yaml（初始值）→ 运行时切换 → 持久化到 model_state.json
模型列表通过 Ollama /api/tags 实时获取

用法:
    from app.model_manager import model_manager
    model_manager.switch_llm("qwen3:14b", temperature=0.3, max_tokens=2048)
    model_manager.switch_embedding("nomic-embed-text:v2", top_k=5)
"""
import json
import logging
import os
from pathlib import Path
from typing import Optional

import httpx
import yaml
from app.config import settings, _CONFIG_PATH

logger = logging.getLogger(__name__)

# 持久化文件路径
_STATE_FILE = Path(__file__).parent.parent / "model_state.json"


class ModelManager:
    """运行时模型配置管理器（单例）"""

    _instance: Optional["ModelManager"] = None

    def __init__(self):
        # LLM 配置（初始值来自 config.yaml，运行时可能被持久化文件覆盖）
        self._llm_model: str = settings.LLM_MODEL
        self._llm_base_url: str = settings.LLM_BASE_URL
        self._llm_api_key: str = settings.LLM_API_KEY
        self._llm_temperature: float = settings.LLM_TEMPERATURE
        self._llm_max_tokens: int = settings.LLM_MAX_TOKENS

        # Embedding 配置
        self._embedding_model: str = settings.EMBEDDING_MODEL
        self._embedding_base_url: str = settings.EMBEDDING_BASE_URL or settings.LLM_BASE_URL
        self._embedding_api_key: str = settings.EMBEDDING_API_KEY
        self._embedding_top_k: int = settings.EMBEDDING_TOP_K

        # 模型变更回调（预留）
        self._on_switch_callbacks: list = []

        # 从磁盘恢复上次保存的配置
        self._load_state()

        logger.info(
            f"[ModelManager] 初始化: LLM={self._llm_model} (temp={self._llm_temperature}, max_tokens={self._llm_max_tokens}), "
            f"Embedding={self._embedding_model} (top_k={self._embedding_top_k})"
        )

    @classmethod
    def get_instance(cls) -> "ModelManager":
        if cls._instance is None:
            cls._instance = cls()
        return cls._instance

    # ── 持久化 ──

    def _save_state(self):
        """将当前运行时配置写入磁盘（model_state.json + config.yaml）"""
        try:
            state = {
                "llm_model": self._llm_model,
                "llm_base_url": self._llm_base_url,
                "llm_temperature": self._llm_temperature,
                "llm_max_tokens": self._llm_max_tokens,
                "embedding_model": self._embedding_model,
                "embedding_base_url": self._embedding_base_url,
                "embedding_top_k": self._embedding_top_k,
            }
            _STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
            with open(_STATE_FILE, "w", encoding="utf-8") as f:
                json.dump(state, f, ensure_ascii=False, indent=2)

            # 同步更新 config.yaml
            if _CONFIG_PATH.exists():
                with open(_CONFIG_PATH, "r", encoding="utf-8") as f:
                    yaml_config = yaml.safe_load(f) or {}
                yaml_config.setdefault("llm", {})["model"] = self._llm_model
                yaml_config.setdefault("llm", {})["base_url"] = self._llm_base_url
                yaml_config.setdefault("llm", {})["temperature"] = self._llm_temperature
                yaml_config.setdefault("llm", {})["max_tokens"] = self._llm_max_tokens
                yaml_config.setdefault("embedding", {})["model"] = self._embedding_model
                yaml_config.setdefault("embedding", {})["base_url"] = self._embedding_base_url
                yaml_config.setdefault("embedding", {})["top_k"] = self._embedding_top_k
                with open(_CONFIG_PATH, "w", encoding="utf-8") as f:
                    yaml.dump(yaml_config, f, allow_unicode=True, default_flow_style=False, sort_keys=False)

            logger.info(f"[ModelManager] 配置已持久化到 {_STATE_FILE} 和 config.yaml")
        except Exception as e:
            logger.warning(f"[ModelManager] 持久化失败: {e}")

    def _load_state(self):
        """从磁盘恢复上次保存的配置"""
        if not _STATE_FILE.exists():
            return
        try:
            with open(_STATE_FILE, "r", encoding="utf-8") as f:
                state = json.load(f)
            self._llm_model = state.get("llm_model", self._llm_model)
            self._llm_base_url = state.get("llm_base_url", self._llm_base_url)
            self._llm_temperature = state.get("llm_temperature", self._llm_temperature)
            self._llm_max_tokens = state.get("llm_max_tokens", self._llm_max_tokens)
            self._embedding_model = state.get("embedding_model", self._embedding_model)
            self._embedding_base_url = state.get("embedding_base_url", self._embedding_base_url)
            self._embedding_top_k = state.get("embedding_top_k", self._embedding_top_k)
            logger.info(f"[ModelManager] 已从磁盘恢复配置: LLM={self._llm_model}, Embedding={self._embedding_model}")
        except Exception as e:
            logger.warning(f"[ModelManager] 恢复配置失败: {e}")

    # ── LLM 配置 ──

    @property
    def llm_model(self) -> str:
        return self._llm_model

    @property
    def llm_base_url(self) -> str:
        return self._llm_base_url

    @property
    def llm_api_key(self) -> str:
        return self._llm_api_key

    @property
    def llm_temperature(self) -> float:
        return self._llm_temperature

    @property
    def llm_max_tokens(self) -> int:
        return self._llm_max_tokens

    def switch_llm(
        self,
        model: str,
        base_url: str = None,
        api_key: str = None,
        temperature: float = None,
        max_tokens: int = None,
    ) -> str:
        """热切换 LLM 模型"""
        old_model = self._llm_model
        self._llm_model = model
        if base_url:
            self._llm_base_url = base_url.rstrip("/")
        if api_key is not None:
            self._llm_api_key = api_key
        if temperature is not None:
            self._llm_temperature = temperature
        if max_tokens is not None:
            self._llm_max_tokens = max_tokens

        self._save_state()
        logger.info(
            f"[ModelManager] LLM 已切换: {old_model} → {model} "
            f"(temp={self._llm_temperature}, max_tokens={self._llm_max_tokens})"
        )
        self._notify_switch("llm", old_model, model)
        return model

    # ── Embedding 配置 ──

    @property
    def embedding_model(self) -> str:
        return self._embedding_model

    @property
    def embedding_base_url(self) -> str:
        return self._embedding_base_url

    @property
    def embedding_api_key(self) -> str:
        return self._embedding_api_key

    @property
    def embedding_top_k(self) -> int:
        return self._embedding_top_k

    @property
    def rag_top_k(self) -> int:
        """重排序后保留的文档数 = embedding_top_k"""
        return self._embedding_top_k

    @property
    def rag_initial_top_k(self) -> int:
        """重排序前的初始检索数 = 2 * embedding_top_k"""
        return max(self._embedding_top_k * 2, 2)

    def switch_embedding(
        self,
        model: str,
        base_url: str = None,
        api_key: str = None,
        top_k: int = None,
    ) -> str:
        """热切换嵌入模型"""
        old_model = self._embedding_model
        self._embedding_model = model
        if base_url:
            self._embedding_base_url = base_url.rstrip("/")
        if api_key is not None:
            self._embedding_api_key = api_key
        if top_k is not None:
            self._embedding_top_k = top_k

        self._save_state()
        logger.info(
            f"[ModelManager] Embedding 已切换: {old_model} → {model} "
            f"(top_k={self._embedding_top_k})"
        )
        self._notify_switch("embedding", old_model, model)
        return model

    # ── 工具方法 ──

    def get_llm_config(self) -> dict:
        return {
            "model": self._llm_model,
            "base_url": self._llm_base_url,
            "api_key": "****" if self._llm_api_key else "",
            "temperature": self._llm_temperature,
            "max_tokens": self._llm_max_tokens,
        }

    def get_embedding_config(self) -> dict:
        return {
            "model": self._embedding_model,
            "base_url": self._embedding_base_url,
            "api_key": "****" if self._embedding_api_key else "",
            "top_k": self._embedding_top_k,
        }

    def get_full_config(self) -> dict:
        return {
            "llm": self.get_llm_config(),
            "embedding": self.get_embedding_config(),
        }

    async def list_ollama_models(self, base_url: str = None) -> list[dict]:
        """列出 Ollama 服务上所有可用模型"""
        url_base = (base_url or self._llm_base_url).rstrip("/")
        if url_base.endswith("/v1"):
            url_base = url_base[:-3]

        try:
            async with httpx.AsyncClient(trust_env=False, timeout=10) as client:
                resp = await client.get(f"{url_base}/api/tags")
                resp.raise_for_status()
                data = resp.json()
            models = data.get("models", [])
            return [{"name": m.get("name", "?"), "size": m.get("size", 0)} for m in models]
        except Exception as e:
            logger.warning(f"[ModelManager] 获取Ollama模型列表失败: {e}")
            return []

    # ── 回调 ──

    def on_switch(self, callback):
        """注册模型切换回调"""
        self._on_switch_callbacks.append(callback)

    def _notify_switch(self, model_type: str, old: str, new: str):
        for cb in self._on_switch_callbacks:
            try:
                cb(model_type, old, new)
            except Exception as e:
                logger.warning(f"[ModelManager] 回调异常: {e}")


# 全局单例
model_manager = ModelManager.get_instance()
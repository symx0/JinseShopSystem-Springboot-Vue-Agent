"""配置加载模块，支持YAML配置文件 + 环境变量覆盖"""
import os
from pathlib import Path

import yaml
from dotenv import load_dotenv

load_dotenv()

# 配置文件路径
_CONFIG_PATH = Path(__file__).parent.parent / "config.yaml"


def _load_yaml_config() -> dict:
    """加载YAML配置文件"""
    if _CONFIG_PATH.exists():
        with open(_CONFIG_PATH, "r", encoding="utf-8") as f:
            return yaml.safe_load(f) or {}
    return {}


_yaml_config = _load_yaml_config()


class Settings:
    """应用配置，优先级：环境变量 > YAML配置 > 默认值"""

    # LLM配置
    LLM_API_KEY: str = os.getenv("LLM_API_KEY") or _yaml_config.get("llm", {}).get("api_key", "")
    LLM_BASE_URL: str = os.getenv("LLM_BASE_URL") or _yaml_config.get("llm", {}).get("base_url", "https://api.openai.com/v1")
    LLM_MODEL: str = os.getenv("LLM_MODEL") or _yaml_config.get("llm", {}).get("model", "gpt-4o-mini")
    LLM_TEMPERATURE: float = float(os.getenv("LLM_TEMPERATURE", "0") or _yaml_config.get("llm", {}).get("temperature", 0.7))
    LLM_MAX_TOKENS: int = int(os.getenv("LLM_MAX_TOKENS", "0") or _yaml_config.get("llm", {}).get("max_tokens", 4096))

    # 服务配置
    HOST: str = os.getenv("HOST") or _yaml_config.get("server", {}).get("host", "0.0.0.0")
    _port_env = os.getenv("PORT")
    PORT: int = int(_port_env) if _port_env else _yaml_config.get("server", {}).get("port", 8000)

    # MCP Server配置
    MCP_COMMAND: str = _yaml_config.get("mcp_server", {}).get("command", "python")
    MCP_CWD: str = _yaml_config.get("mcp_server", {}).get("cwd", "../FlowerShopMCPService")
    MCP_MODULE_PATH: str = _yaml_config.get("mcp_server", {}).get("module_path", "")

    # 嵌入模型配置（由 Agent 端 model_manager 管理，直连 Ollama）
    EMBEDDING_MODE: str = os.getenv("EMBEDDING_MODE") or _yaml_config.get("embedding", {}).get("mode", "tfidf")
    EMBEDDING_BASE_URL: str = os.getenv("EMBEDDING_BASE_URL") or _yaml_config.get("embedding", {}).get("base_url", "")
    EMBEDDING_API_KEY: str = os.getenv("EMBEDDING_API_KEY") or _yaml_config.get("embedding", {}).get("api_key", "")
    EMBEDDING_MODEL: str = os.getenv("EMBEDDING_MODEL") or _yaml_config.get("embedding", {}).get("model", "")
    EMBEDDING_TOP_K: int = int(os.getenv("EMBEDDING_TOP_K", "0") or _yaml_config.get("embedding", {}).get("top_k", 5))


settings = Settings()

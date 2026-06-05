import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    # LLM配置
    LLM_API_KEY: str = os.getenv("LLM_API_KEY", "")
    LLM_BASE_URL: str = os.getenv("LLM_BASE_URL", "https://api.openai.com/v1")
    LLM_MODEL: str = os.getenv("LLM_MODEL", "gpt-4o-mini")

    # 服务配置
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))

    # 知识库路径
    KNOWLEDGE_DIR: str = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data", "knowledge")


settings = Settings()

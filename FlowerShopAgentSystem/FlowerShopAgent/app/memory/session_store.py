"""
会话持久化与生命周期管理模块

- 会话状态以 DB 自增主键命名 JSON 文件持久化到磁盘
- 提供按需加载（懒加载）+ 内存缓存
- 统一管理会话创建、获取、保存、删除
"""
import json
import logging
import time
from pathlib import Path
from typing import Optional

from langchain_core.messages import HumanMessage, AIMessage, SystemMessage, ToolMessage, BaseMessage

logger = logging.getLogger(__name__)

# 会话文件存储目录
_SESSION_DIR = Path(__file__).parent.parent / "data" / "sessions"

# 对话历史保留上限（条消息）
_MAX_MESSAGES = 60


# ═══════════════════════════════════════════
#  序列化 / 反序列化
# ═══════════════════════════════════════════

def _serialize_message(msg: BaseMessage) -> dict:
    msg_type = "unknown"
    if isinstance(msg, HumanMessage):
        msg_type = "human"
    elif isinstance(msg, AIMessage):
        msg_type = "ai"
    elif isinstance(msg, SystemMessage):
        msg_type = "system"
    elif isinstance(msg, ToolMessage):
        msg_type = "tool"
    result = {
        "type": msg_type,
        "content": msg.content if isinstance(msg.content, str) else str(msg.content),
    }
    if isinstance(msg, ToolMessage):
        result["tool_call_id"] = msg.tool_call_id
        result["name"] = msg.name
    if isinstance(msg, AIMessage) and msg.tool_calls:
        result["tool_calls"] = msg.tool_calls
    return result


def _deserialize_message(data: dict) -> BaseMessage:
    msg_type = data.get("type", "human")
    content = data.get("content", "")
    if msg_type == "human":
        return HumanMessage(content=content)
    elif msg_type == "ai":
        ai_msg = AIMessage(content=content)
        if data.get("tool_calls"):
            ai_msg.tool_calls = data["tool_calls"]
        return ai_msg
    elif msg_type == "system":
        return SystemMessage(content=content)
    elif msg_type == "tool":
        return ToolMessage(
            content=content,
            tool_call_id=data.get("tool_call_id", ""),
            name=data.get("name", ""),
        )
    else:
        return HumanMessage(content=content)


def _migrate_thinking_chain(value) -> str:
    """兼容旧格式：旧版 thinking_chain 为 list，新版为 str（会话总结）"""
    if isinstance(value, list):
        return "\n".join(value) if value else ""
    return value or ""


def _serialize_state(state: dict) -> dict:
    messages = state.get("messages", [])
    if len(messages) > _MAX_MESSAGES:
        messages = messages[-_MAX_MESSAGES:]
    return {
        "session_id": state.get("session_id", ""),
        "user_id": state.get("user_id"),
        "messages": [_serialize_message(m) for m in messages],
        "identified_scenario": state.get("identified_scenario"),
        "scenario_name": state.get("scenario_name"),
        "scenario_id": state.get("scenario_id"),
        "scenario_confidence": state.get("scenario_confidence", 0.0),
        "extracted_keywords": state.get("extracted_keywords", []),
        "budget_min": state.get("budget_min"),
        "budget_max": state.get("budget_max"),
        "preferred_category": state.get("preferred_category"),
        "recipient": state.get("recipient"),
        "special_requirements": state.get("special_requirements"),
        "current_step": state.get("current_step", "analyze"),
        "user_type": state.get("user_type", "shopping"),
        "rag_documents": state.get("rag_documents", []),
        "_hallucination_flags": state.get("_hallucination_flags", []),
        "_order_valid": state.get("_order_valid", True),
        "_recommend_failed_rounds": state.get("_recommend_failed_rounds", 0),
        "_clarify_round": state.get("_clarify_round", 0),
        "thinking_chain": state.get("thinking_chain", ""),
        "last_active": time.time(),
    }


def _deserialize_state(data: dict) -> dict:
    messages = [_deserialize_message(m) for m in data.get("messages", [])]
    return {
        "session_id": data.get("session_id", ""),
        "user_id": data.get("user_id"),
        "messages": messages,
        "user_input": data.get("user_input", ""),
        "identified_scenario": data.get("identified_scenario"),
        "scenario_name": data.get("scenario_name"),
        "scenario_id": data.get("scenario_id"),
        "scenario_confidence": data.get("scenario_confidence", 0.0),
        "extracted_keywords": data.get("extracted_keywords", []),
        "budget_min": data.get("budget_min"),
        "budget_max": data.get("budget_max"),
        "preferred_category": data.get("preferred_category"),
        "recipient": data.get("recipient"),
        "special_requirements": data.get("special_requirements"),
        "order": None,
        "current_step": data.get("current_step", "classify"),
        "user_type": data.get("user_type", "shopping"),
        "rag_documents": data.get("rag_documents", []),
        "_raw_products": None,
        "_raw_activities": None,
        "_raw_knowledge": None,
        "_hallucination_flags": data.get("_hallucination_flags", []),
        "_order_valid": data.get("_order_valid", True),
        "_recommend_failed_rounds": data.get("_recommend_failed_rounds", 0),
        "_clarify_round": data.get("_clarify_round", 0),
        "thinking_chain": _migrate_thinking_chain(data.get("thinking_chain", "")),
    }


# ═══════════════════════════════════════════
#  会话管理器
# ═══════════════════════════════════════════

class SessionManager:
    """
    会话生命周期管理器

    - _sessions: 内存缓存层，启动时为空，按需加载
    - 磁盘: data/sessions/{session_id}.json（session_id = DB 自增主键）
    """

    def __init__(self):
        self._sessions: dict[str, dict] = {}   # 内存缓存
        self._disk_cache: dict[str, dict] = {} # 磁盘读取缓存（同 SessionStore 原实现）
        _SESSION_DIR.mkdir(parents=True, exist_ok=True)

    # ── 磁盘读写 ──

    def _file_path(self, session_id: str) -> Path:
        return _SESSION_DIR / f"{session_id}.json"

    def _load_from_disk(self, session_id: str) -> Optional[dict]:
        """从磁盘读取会话状态"""
        if session_id in self._disk_cache:
            return self._disk_cache[session_id]
        path = self._file_path(session_id)
        if path.exists():
            logger.debug(f"从磁盘加载会话({session_id})")
            try:
                with open(path, "r", encoding="utf-8") as f:
                    data = json.load(f)
                state = _deserialize_state(data)
                self._disk_cache[session_id] = state
                return state
            except Exception as e:
                logger.warning(f"加载会话文件失败({session_id}): {e}")
        return None

    def _save_to_disk(self, session_id: str, state: dict):
        """保存会话状态到磁盘"""
        self._disk_cache[session_id] = state
        path = self._file_path(session_id)
        try:
            data = _serialize_state(state)
            with open(path, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.warning(f"保存会话失败({session_id}): {e}")

    def _delete_from_disk(self, session_id: str):
        """删除磁盘上的会话文件"""
        self._disk_cache.pop(session_id, None)
        path = self._file_path(session_id)
        if path.exists():
            try:
                path.unlink()
            except Exception as e:
                logger.warning(f"删除会话文件失败({session_id}): {e}")

    # ── 新会话状态工厂 ──

    @staticmethod
    def new_state(session_id: str, user_id: str = None) -> dict:
        """创建空白会话状态"""
        return {
            "session_id": session_id,
            "user_id": user_id,
            "messages": [],
            "user_input": "",
            "user_type": "shopping",
            "intent_confidence": 0.5,
            "extracted_keywords": [],
            "rag_documents": [],
            "identified_scenario": None,
            "scenario_name": None,
            "scenario_id": None,
            "scenario_confidence": 0.0,
            "budget_min": None,
            "budget_max": None,
            "preferred_category": None,
            "recipient": None,
            "special_requirements": None,
            "order": None,
            "_raw_products": None,
            "_raw_activities": None,
            "_raw_knowledge": None,
            "_hallucination_flags": [],
            "_order_valid": True,
            "_recommend_failed_rounds": 0,
            "_clarify_round": 0,
            "current_step": "classify",
            "thinking_chain": "",
        }

    # ── 核心 API ──

    def get(self, session_id: str) -> Optional[dict]:
        """获取会话状态（内存缓存 → 磁盘文件），不存在返回 None"""
        if session_id in self._sessions:
            return self._sessions[session_id]
        state = self._load_from_disk(session_id)
        if state:
            self._sessions[session_id] = state
        return state

    def get_or_create(self, session_id: str, user_id: str = None) -> dict:
        """获取或创建会话（内存 → 磁盘 → 新建）"""
        state = self.get(session_id)
        if state:
            return state
        state = self.new_state(session_id, user_id=user_id)
        self._sessions[session_id] = state
        return state

    def put(self, session_id: str, state: dict):
        """更新内存中的会话状态（不自动写磁盘，调用 save 才持久化）"""
        self._sessions[session_id] = state

    def save(self, session_id: str, state: dict = None):
        """持久化会话到磁盘"""
        if state is not None:
            self._sessions[session_id] = state
        s = self._sessions.get(session_id)
        if s:
            self._save_to_disk(session_id, s)

    def reset(self, session_id: str):
        """重置会话：清除内存 + 删除磁盘文件"""
        self._sessions.pop(session_id, None)
        self._delete_from_disk(session_id)

    def save_all(self):
        """保存所有内存中的会话到磁盘"""
        for sid in list(self._sessions.keys()):
            self._save_to_disk(sid, self._sessions[sid])

    def clear(self):
        """清空内存缓存（不删磁盘文件）"""
        self._sessions.clear()

    def keys(self):
        return self._sessions.keys()

    def items(self):
        return self._sessions.items()


# 全局单例
session_manager = SessionManager()

# 兼容旧代码的别名
session_store = session_manager
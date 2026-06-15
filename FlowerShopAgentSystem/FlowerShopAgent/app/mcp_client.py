"""
MCP 客户端模块
Agent通过此客户端连接MCP Server，调用工具和读取资源

支持两种模式:
1. MCP stdio模式 - 启动MCP Server子进程，通过stdio通信（推荐）
2. 降级模式 - 当MCP不可用时使用本地知识库
"""
import json
import logging
import os
from contextlib import asynccontextmanager
from pathlib import Path
from typing import Optional

logger = logging.getLogger(__name__)


class MCPClient:
    """
    MCP 客户端封装
    管理与MCP Server的连接，提供工具调用和资源读取接口
    """

    def __init__(self, config):
        self.config = config
        self._session = None
        self._stdio_context = None
        self._read = None
        self._write = None
        self._available = False
        self._tools: dict[str, callable] = {}
        self._tools_list = []

    @property
    def is_available(self) -> bool:
        return self._available

    def get_ollama_tools(self, tool_names: list[str] = None) -> list[dict]:
        """
        将 MCP Server 的工具定义转换为 Ollama /api/chat 的 tools 格式。
        动态获取，无需硬编码。

        参数:
            tool_names: 可选，只返回指定名称的工具。为 None 时返回全部工具。

        返回:
            list[dict]: Ollama 格式的工具定义列表
        """
        if not self._available or not self._tools_list:
            return []

        ollama_tools = []
        for t in self._tools_list:
            if tool_names and t.name not in tool_names:
                continue
            # MCP tool 对象有 name, description, inputSchema 属性
            tool_def = {
                "type": "function",
                "function": {
                    "name": t.name,
                    "description": t.description or "",
                    "parameters": t.inputSchema if t.inputSchema else {"type": "object", "properties": {}},
                },
            }
            ollama_tools.append(tool_def)

        return ollama_tools

    async def connect(self) -> bool:
        """
        连接MCP Server（stdio模式）
        返回是否连接成功
        """
        try:
            # 兼容不同版本的mcp库
            try:
                from mcp.client.session import ClientSession
            except ImportError:
                from mcp.client import ClientSession

            from mcp.client.stdio import stdio_client, StdioServerParameters

            # 确定MCP Server的工作目录
            cwd = self.config.MCP_CWD
            if cwd and not os.path.isabs(cwd):
                cwd = str(Path(__file__).parent.parent / cwd)

            # 构建启动参数
            module_path = self.config.MCP_MODULE_PATH
            if module_path and not os.path.isabs(module_path):
                # 相对于 cwd 解析
                module_path = str(Path(cwd) / module_path)
            server_params = StdioServerParameters(
                command=self.config.MCP_COMMAND,
                args=[module_path] if module_path else [],
                cwd=cwd,
            )

            logger.info(f"正在连接MCP Server: {server_params.command} {' '.join(server_params.args)}")

            # 建立stdio连接
            self._stdio_context = stdio_client(server_params)
            read_stream, write_stream = await self._stdio_context.__aenter__()
            self._read = read_stream
            self._write = write_stream

            # 创建会话
            self._session_context = ClientSession(read_stream, write_stream)
            self._session = await self._session_context.__aenter__()
            await self._session.initialize()

            # 列出可用工具并缓存
            tools_result = await self._session.list_tools()
            self._tools_list = tools_result.tools
            logger.info(f"MCP Server连接成功，可用工具: {[t.name for t in tools_result.tools]}")

            self._available = True
            return True

        except ImportError as e:
            logger.warning(f"MCP库不可用: {e}，将使用降级模式")
            return False
        except Exception as e:
            logger.warning(f"MCP Server连接失败: {e}，将使用降级模式")
            return False

    async def call_tool(self, tool_name: str, arguments: dict = None) -> str:
        """
        调用MCP工具
        返回工具执行结果的字符串
        """
        if not self._available or not self._session:
            raise RuntimeError("MCP Server未连接")

        arguments = arguments or {}
        logger.info(f"[MCP] 调用工具: {tool_name}, 参数: {json.dumps(arguments, ensure_ascii=False)[:200]}")

        try:
            result = await self._session.call_tool(tool_name, arguments)
        except Exception as e:
            logger.error(f"[MCP] 工具调用异常: {tool_name}, 错误: {e}")
            raise

        # 从 CallToolResult 中提取文本内容
        if hasattr(result, 'content') and result.content:
            texts = []
            for item in result.content:
                if hasattr(item, 'text'):
                    texts.append(item.text)
                else:
                    texts.append(str(item))
            response_text = "\n".join(texts) if texts else ""
        else:
            response_text = str(result)

        # 日志输出返回信息（截断过长内容）
        log_preview = response_text[:300] + ("..." if len(response_text) > 300 else "")
        logger.info(f"[MCP] 工具返回: {tool_name}, 长度={len(response_text)}, 预览={log_preview}")

        return response_text

    async def read_resource(self, uri: str) -> str:
        """
        读取MCP资源
        返回资源内容的字符串
        """
        if not self._available or not self._session:
            raise RuntimeError("MCP Server未连接")

        logger.info(f"读取MCP资源: {uri}")
        result = await self._session.read_resource(uri)

        # 从 ReadResourceResult 中提取文本内容
        if hasattr(result, 'content') and result.content:
            texts = []
            for item in result.content:
                if hasattr(item, 'text'):
                    texts.append(item.text)
                else:
                    texts.append(str(item))
            return "\n".join(texts) if texts else ""

        return str(result)

    async def disconnect(self):
        """断开MCP连接"""
        try:
            if self._session_context:
                await self._session_context.__aexit__(None, None, None)
            if self._stdio_context:
                await self._stdio_context.__aexit__(None, None, None)
        except Exception as e:
            logger.warning(f"MCP断开连接异常: {e}")
        finally:
            self._available = False
            self._session = None


# 全局MCP客户端实例
_mcp_client: Optional[MCPClient] = None


def get_mcp_client() -> MCPClient:
    """获取全局MCP客户端实例"""
    global _mcp_client
    if _mcp_client is None:
        from app.config import settings
        _mcp_client = MCPClient(settings)
    return _mcp_client


async def init_mcp() -> bool:
    """初始化MCP连接"""
    client = get_mcp_client()
    return await client.connect()


async def shutdown_mcp():
    """关闭MCP连接"""
    client = get_mcp_client()
    await client.disconnect()

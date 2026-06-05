import json
import logging
from typing import Optional

from app.config import settings
from app.models.schemas import Scenario

logger = logging.getLogger(__name__)

_scenarios: list[Scenario] = []
_scenarios_by_id: dict[str, Scenario] = {}


def load_scenarios() -> list[Scenario]:
    """从JSON文件加载场景数据"""
    file_path = f"{settings.KNOWLEDGE_DIR}/scenarios.json"
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        scenarios = [Scenario(**item) for item in data]
        logger.info(f"成功加载 {len(scenarios)} 个场景")
        return scenarios
    except FileNotFoundError:
        logger.warning(f"场景数据文件不存在: {file_path}")
        return []
    except Exception as e:
        logger.error(f"加载场景数据失败: {e}")
        return []


def init_scenario_kb() -> None:
    """初始化场景知识库"""
    global _scenarios, _scenarios_by_id
    _scenarios = load_scenarios()
    _scenarios_by_id = {s.id: s for s in _scenarios}


def get_all_scenarios() -> list[Scenario]:
    """获取所有场景"""
    return _scenarios


def get_scenario_by_id(scenario_id: str) -> Optional[Scenario]:
    """根据ID获取场景"""
    return _scenarios_by_id.get(scenario_id)


def match_scenario(text: str) -> list[tuple[Scenario, int]]:
    """根据文本匹配场景，返回(场景, 匹配关键词数)列表，按匹配数降序排列"""
    text_lower = text.lower()
    results = []
    for scenario in _scenarios:
        match_count = sum(1 for kw in scenario.keywords if kw in text_lower)
        if match_count > 0:
            results.append((scenario, match_count))
    results.sort(key=lambda x: x[1], reverse=True)
    return results

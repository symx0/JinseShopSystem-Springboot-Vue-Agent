import json
import logging
from typing import Optional

from app.config import settings
from app.models.schemas import Product

logger = logging.getLogger(__name__)

_products: list[Product] = []
_products_by_id: dict[int, Product] = {}
_products_by_category: dict[str, list[Product]] = {}


def load_products() -> list[Product]:
    """从JSON文件加载商品数据"""
    file_path = f"{settings.KNOWLEDGE_DIR}/products.json"
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        products = [Product(**item) for item in data]
        logger.info(f"成功加载 {len(products)} 个商品")
        return products
    except FileNotFoundError:
        logger.warning(f"商品数据文件不存在: {file_path}")
        return []
    except Exception as e:
        logger.error(f"加载商品数据失败: {e}")
        return []


def init_product_kb() -> None:
    """初始化商品知识库"""
    global _products, _products_by_id, _products_by_category
    _products = load_products()
    _products_by_id = {p.id: p for p in _products}
    _products_by_category = {}
    for p in _products:
        _products_by_category.setdefault(p.category, []).append(p)


def get_all_products() -> list[Product]:
    """获取所有在售商品"""
    return [p for p in _products if p.status == 1]


def get_product_by_id(product_id: int) -> Optional[Product]:
    """根据ID获取商品"""
    return _products_by_id.get(product_id)


def get_products_by_category(category: str) -> list[Product]:
    """根据分类获取商品"""
    return [p for p in _products_by_category.get(category, []) if p.status == 1]


def search_products(query: str) -> list[Product]:
    """根据关键词搜索商品（名称、描述、标签、场景）"""
    query_lower = query.lower()
    results = []
    for p in _products:
        if p.status != 1:
            continue
        if (query_lower in p.name.lower()
                or query_lower in p.description.lower()
                or any(query_lower in tag.lower() for tag in p.tags)
                or any(query_lower in s.lower() for s in p.applicable_scenarios)):
            results.append(p)
    return results


def get_products_by_scenario(scenario_id: str) -> list[Product]:
    """根据场景ID获取推荐商品"""
    results = []
    for p in _products:
        if p.status != 1:
            continue
        if scenario_id in p.applicable_scenarios:
            results.append(p)
    return results


def get_products_by_tags(tags: list[str]) -> list[Product]:
    """根据标签筛选商品（匹配任一标签）"""
    if not tags:
        return get_all_products()
    tags_lower = [t.lower() for t in tags]
    results = []
    for p in _products:
        if p.status != 1:
            continue
        if any(tag in [t.lower() for t in p.tags] for tag in tags_lower):
            results.append(p)
    return results


def get_products_in_price_range(min_price: float = 0, max_price: float = float("inf")) -> list[Product]:
    """根据价格区间筛选商品"""
    return [p for p in _products if p.status == 1 and min_price <= float(p.price) <= max_price]

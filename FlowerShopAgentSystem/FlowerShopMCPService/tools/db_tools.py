"""
MCP Server 数据库工具模块
提供查询MySQL数据库的能力：鲜花、活动、分类、订单等
"""
import logging
from typing import Optional

from decimal import Decimal

import pymysql
from dbutils.pooled_db import PooledDB

logger = logging.getLogger(__name__)

# 全局连接池
_pool: Optional[PooledDB] = None


def init_db_pool(config: dict) -> PooledDB:
    """初始化数据库连接池"""
    logger.info("初始化数据库连接池")
    global _pool
    mysql_cfg = config["mysql"]
    _pool = PooledDB(
        creator=pymysql,
        maxconnections=mysql_cfg.get("pool_size", 5),
        mincached=1,
        maxcached=3,
        blocking=True,
        host=mysql_cfg["host"],
        port=mysql_cfg["port"],
        user=mysql_cfg["user"],
        password=mysql_cfg["password"],
        database=mysql_cfg["database"],
        charset=mysql_cfg.get("charset", "utf8mb4"),
        cursorclass=pymysql.cursors.DictCursor,
    )
    logger.info(f"数据库连接池初始化完成: {mysql_cfg['host']}:{mysql_cfg['port']}/{mysql_cfg['database']}")
    return _pool


def _query(sql: str, params: tuple = None) -> list[dict]:
    """执行查询SQL，返回字典列表（Decimal 自动转为 float）"""
    conn = _pool.connection()
    try:
        with conn.cursor() as cursor:
            cursor.execute(sql, params)
            rows = cursor.fetchall()
        # MySQL DECIMAL → Python Decimal，JSON 无法序列化，转为 float
        for row in rows:
            for k, v in row.items():
                if isinstance(v, Decimal):
                    row[k] = float(v)
        return rows
    finally:
        conn.close()


def search_flowers(
        keyword: str = None,
        category_id: int = None,
        min_price: float = None,
        max_price: float = None,
        limit: int = 20,
) -> list[dict]:
    """
    搜索鲜花商品，支持关键词、分类、价格区间过滤（仅查询上架商品）
    """
    conditions = ["f.status = 1"]  # 硬编码为上架状态
    params = []  # 不再需要 status 参数
    logger.info("条件查询鲜花商品")

    if keyword:
        conditions.append("(f.name LIKE %s OR f.description LIKE %s)")
        kw = f"%{keyword}%"
        params.extend([kw, kw])

    if category_id:
        conditions.append("f.category_id = %s")
        params.append(category_id)

    if min_price is not None:
        conditions.append("f.price >= %s")
        params.append(min_price)

    if max_price is not None:
        conditions.append("f.price <= %s")
        params.append(max_price)

    where = " AND ".join(conditions)
    sql = f""" 
        SELECT f.id, f.name, f.category_id, f.price, f.image, f.description, f.status, 
               c.name AS category_name 
        FROM flower f 
        LEFT JOIN category c ON f.category_id = c.id 
        WHERE {where} 
        ORDER BY f.price ASC 
        LIMIT %s 
    """
    params.append(limit)
    return _query(sql, tuple(params))


def get_flower_by_id(flower_id: int) -> Optional[dict]:
    """根据ID查询鲜花详情（含活动促销信息），仅返回上架且有库存的鲜花"""
    logger.info(f"开始查询鲜花详情: {flower_id}")
    sql = """ 
        SELECT f.id, f.name, f.category_id, f.price, f.image, f.description, f.status, 
               c.name AS category_name, 
               s.id AS sale_id, s.original_price, s.discount_price, s.stock, s.sale, 
               a.limit_per, a.content AS activity_content 
        FROM flower f 
        LEFT JOIN category c ON f.category_id = c.id 
        LEFT JOIN activity_sale s ON f.id = s.flower_id AND s.stock > 0
        LEFT JOIN activity a ON s.activity_id = a.id 
            AND a.status = 1 AND a.start_time <= NOW() AND a.end_time >= NOW()
        WHERE f.id = %s 
          AND f.status = 1
    """
    rows = _query(sql, (flower_id,))
    return rows[0] if rows else None

def get_ongoing_activities() -> list[dict]:
    """查询当前进行中的活动列表"""
    logger.info("查询当前进行中的活动列表")
    sql = """
        SELECT id, start_time, end_time, status, content, limit_per
        FROM activity
        WHERE status = 1 AND start_time <= NOW() AND end_time >= NOW()
        ORDER BY start_time DESC
    """
    return _query(sql)


def get_activity_flowers(activity_id: int) -> list[dict]:
    """查询活动下的促销鲜花列表"""
    logger.info(f"查询活动 {activity_id} 下的促销鲜花列表")
    sql = """
        SELECT s.id AS sale_id, s.activity_id, s.flower_id, s.original_price,
               s.discount_price, s.stock, s.sale, a.limit_per,
               f.name, f.price, f.image, f.description, f.category_id,
               c.name AS category_name
        FROM activity_sale s
        JOIN flower f ON s.flower_id = f.id AND f.status = 1
        LEFT JOIN category c ON f.category_id = c.id
        LEFT JOIN activity a ON s.activity_id = a.id
        WHERE s.activity_id = %s AND s.stock > 0
        ORDER BY s.discount_price ASC
    """
    return _query(sql, (activity_id,))


def get_all_categories() -> list[dict]:
    """查询所有启用的分类"""
    logger.info("查询所有启用的分类")
    sql = """
        SELECT id, name, sort, image
        FROM category
        WHERE status = 1
        ORDER BY sort ASC
    """
    return _query(sql)


def get_all_flowers_with_promos() -> list[dict]:
    """
    获取所有在售鲜花，连带活动促销信息
    用于Agent构建推荐订单
    """
    logger.info("查询所有在售鲜花，连带活动促销信息")
    sql = """
        SELECT
            f.id, f.name, f.category_id, f.price, f.image, f.description, f.status,
            c.name AS category_name,
            s.id AS sale_id, s.activity_id, s.original_price, s.discount_price,
            s.stock, s.sale, a.limit_per, a.content AS activity_content,
            a.content AS activity_name,
            CASE WHEN s.id IS NOT NULL AND s.stock > 0
                 AND a.status = 1 AND a.start_time <= NOW() AND a.end_time >= NOW()
                 THEN 1 ELSE 0 END AS is_promo
        FROM flower f
        LEFT JOIN category c ON f.category_id = c.id
        LEFT JOIN activity_sale s ON f.id = s.flower_id AND s.stock > 0
        LEFT JOIN activity a ON s.activity_id = a.id
        WHERE f.status = 1
        ORDER BY is_promo DESC, f.price ASC
    """
    return _query(sql)


def get_flower_by_scenario_keywords(keywords: list[str], limit: int = 10) -> list[dict]:
    """
    根据场景关键词匹配鲜花
    关键词匹配鲜花名称、描述、分类名
    """
    logger.info(f"根据场景关键词 {keywords} 匹配鲜花")
    if not keywords:
        return get_all_flowers_with_promos()[:limit]

    like_clauses = []
    params = []
    for kw in keywords:
        like = f"%{kw}%"
        like_clauses.append("(f.name LIKE %s OR f.description LIKE %s OR c.name LIKE %s)")
        params.extend([like, like, like])

    sql = f"""
        SELECT DISTINCT
            f.id, f.name, f.category_id, f.price, f.image, f.description, f.status,
            c.name AS category_name,
            s.id AS sale_id, s.activity_id, s.original_price, s.discount_price,
            s.stock, s.sale, a.limit_per
        FROM flower f
        LEFT JOIN category c ON f.category_id = c.id
        LEFT JOIN activity_sale s ON f.id = s.flower_id AND s.stock > 0
        LEFT JOIN activity a ON s.activity_id = a.id
        WHERE f.status = 1 AND ({' OR '.join(like_clauses)})
        ORDER BY f.price ASC
        LIMIT %s
    """
    params.append(limit)
    return _query(sql, tuple(params))

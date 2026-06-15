"""数据模型定义"""
from decimal import Decimal
from typing import Optional

from pydantic import BaseModel, Field


# ═══════════════════════════════════════════
#  订单相关
# ═══════════════════════════════════════════

class ProductInfo(BaseModel):
    """商品展示信息（来自数据库）"""
    id: int
    name: str
    category_id: Optional[int] = None
    category_name: Optional[str] = None
    price: float
    image: Optional[str] = None
    description: Optional[str] = None
    is_promo: bool = False
    sale_id: Optional[int] = None
    activity_id: Optional[int] = None
    original_price: Optional[float] = None
    discount_price: Optional[float] = None
    stock: Optional[int] = None
    limit_per: Optional[int] = None
    activity_content: Optional[str] = None


class OrderItem(BaseModel):
    """订单商品项（Agent 只负责推荐 flower_id + 数量，详情由前端从 Spring Boot 获取）"""
    flower_id: int
    quantity: int = 1


class RecommendedOrder(BaseModel):
    """Agent推荐的订单摘要"""
    items: list[OrderItem] = Field(default_factory=list, description="推荐商品ID和数量")
    scenario: str = ""
    summary: str = ""
    tips: str = ""


# ═══════════════════════════════════════════
#  API 请求/响应
# ═══════════════════════════════════════════

class ChatRequest(BaseModel):
    session_id: str
    message: str
    user_id: Optional[str] = None  # 用户ID

    model_config = {"populate_by_name": True}

    def __init__(self, **data):
        # 兼容前端传int类型的user_id
        if data.get("user_id") is not None:
            data["user_id"] = str(data["user_id"])
        super().__init__(**data)


class ChatResponse(BaseModel):
    reply: str
    session_id: str
    order: Optional[RecommendedOrder] = None
    action_buttons: list[dict] = Field(default_factory=list)
    current_step: str = "analyze"
    thinking_chain: str = Field(default="", description="会话总结（LLM对过往问答的总结）")


class OrderUpdateRequest(BaseModel):
    session_id: str
    action: str                      # add | remove | update_quantity | replace | clear
    product_id: Optional[int] = None
    quantity: Optional[int] = None
    new_product_id: Optional[int] = None


class OrderConfirmRequest(BaseModel):
    session_id: str
    user_id: Optional[int] = None    # 用户ID，MCP需要
    address_book_id: Optional[int] = None
    pay_method: int = 1
    remark: str = ""
    delivery_status: int = 1
    estimated_delivery_time: Optional[str] = None

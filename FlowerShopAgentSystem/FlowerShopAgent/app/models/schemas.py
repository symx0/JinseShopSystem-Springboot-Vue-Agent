from pydantic import BaseModel, Field
from typing import Optional
from decimal import Decimal


class Product(BaseModel):
    """商品信息"""
    id: int = Field(description="商品ID")
    name: str = Field(description="商品名称")
    category: str = Field(description="分类名称")
    category_id: int = Field(description="分类ID")
    price: Decimal = Field(description="价格")
    description: str = Field(default="", description="描述信息")
    image: str = Field(default="", description="图片URL")
    status: int = Field(default=1, description="状态 0停售 1起售")
    tags: list[str] = Field(default_factory=list, description="标签（如：送礼、表白、慰问等）")
    applicable_scenarios: list[str] = Field(default_factory=list, description="适用场景")


class Scenario(BaseModel):
    """购买场景"""
    id: str = Field(description="场景标识")
    name: str = Field(description="场景名称")
    description: str = Field(description="场景描述")
    keywords: list[str] = Field(description="触发关键词")
    recommended_categories: list[str] = Field(description="推荐分类")
    tips: str = Field(default="", description="选购建议")
    budget_suggestion: str = Field(default="", description="预算建议")


class RecommendationItem(BaseModel):
    """推荐商品项"""
    product: Product = Field(description="推荐商品")
    reason: str = Field(description="推荐理由")
    match_score: float = Field(default=0.0, description="匹配度评分 0-1")


class RecommendationPlan(BaseModel):
    """推荐方案"""
    scenario: str = Field(default="", description="识别到的场景")
    summary: str = Field(default="", description="方案概述")
    items: list[RecommendationItem] = Field(default_factory=list, description="推荐商品列表")
    total_price: Optional[Decimal] = Field(default=None, description="方案总价")
    tips: str = Field(default="", description="额外建议")


class ChatRequest(BaseModel):
    """聊天请求"""
    message: str = Field(description="用户消息")
    session_id: str = Field(default="default", description="会话ID")


class ChatResponse(BaseModel):
    """聊天响应"""
    reply: str = Field(description="Agent回复")
    plan: Optional[RecommendationPlan] = Field(default=None, description="推荐方案（如有）")
    need_more_info: bool = Field(default=False, description="是否需要更多信息")
    session_id: str = Field(default="default", description="会话ID")

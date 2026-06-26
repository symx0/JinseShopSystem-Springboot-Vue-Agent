package com.jinse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单消息实体
 * 用于 RocketMQ 消息传递
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息关联ID（用于异步下单结果查询）
     */
    private String correlationId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNumber;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单提交数据（JSON字符串）
     */
    private String payload;

    /**
     * 消息时间戳
     */
    private Long timestamp;
}

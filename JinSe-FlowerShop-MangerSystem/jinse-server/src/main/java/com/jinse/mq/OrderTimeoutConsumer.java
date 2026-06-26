package com.jinse.mq;

import com.alibaba.fastjson.JSON;
import com.jinse.entity.OrderMessage;
import com.jinse.entity.Orders;
import com.jinse.mapper.OrderMapper;
import com.jinse.service.AlipayService;
import com.jinse.service.OrderService;
import com.jinse.utils.DistributedLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 订单超时消费者
 * 收到延迟消息后检查订单是否已付款，未付款则自动取消
 * 使用分布式锁防止与支付回调并发冲突
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "order-timeout-topic", consumerGroup = "order-timeout-group")
public class OrderTimeoutConsumer implements RocketMQListener<String> {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private DistributedLockUtil distributedLockUtil;

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(String message) {
        long startTime = System.currentTimeMillis();
        log.info("[MQ-Consumer-Timeout] 收到订单超时检查消息，topic=order-timeout-topic, 消息体={}", message);
        try {
            // 使用 OrderMessage 实体类解析消息
            OrderMessage orderMessage = JSON.parseObject(message, OrderMessage.class);
            Long orderId = orderMessage.getOrderId();
            String orderNumber = orderMessage.getOrderNumber();
//            log.info("[MQ-Consumer-Timeout] 解析消息成功，orderId={}, orderNumber={}", orderId, orderNumber);

            // 尝试获取分布式锁，防止与支付回调并发
            String lockValue = distributedLockUtil.tryLock(orderId, 15);
            if (lockValue == null) {
                log.info("[MQ-Consumer-Timeout] 获取分布式锁失败，订单 {} 正在被支付回调处理，跳过超时关单", orderNumber);
                return;
            }
            log.info("[MQ-Consumer-Timeout] 获取分布式锁成功，orderId={}, lockValue={}", orderId, lockValue);

            try {
                Orders orders = orderMapper.getById(orderId);
                if (orders == null) {
                    log.warn("[MQ-Consumer-Timeout] 订单不存在，orderId={}", orderId);
                    return;
                }
                log.info("[MQ-Consumer-Timeout] 查询订单成功，orderNumber={}, 当前状态={}", orderNumber, orders.getStatus());

                // 如果订单已不是待付款状态，无需处理
                if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
                    log.info("[MQ-Consumer-Timeout] 订单已处理，无需取消，orderNumber={}, 状态={}", orderNumber, orders.getStatus());
                    return;
                }

                // 取消前先查询支付宝交易状态，防止用户已付款但本地状态未更新
                log.info("[MQ-Consumer-Timeout] 开始查询支付宝交易状态，orderNumber={}", orderNumber);
                String tradeStatus = alipayService.queryTradeStatus(orderNumber);

                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    // 支付宝已确认收款，但本地状态仍为待付款 → 补偿更新为已支付
                    log.info("[MQ-Consumer-Timeout] 支付宝交易已成功，补偿更新订单状态，orderNumber={}", orderNumber);
                    orderService.payment(orders.getId(), orders.getUserId());
                    log.info("[MQ-Consumer-Timeout] 订单状态补偿为已支付，orderNumber={}", orderNumber);
                } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                    // 交易创建但未付款 → 关闭支付宝交易后取消订单
                    log.info("[MQ-Consumer-Timeout] 交易待付款，关闭交易并取消订单，orderNumber={}", orderNumber);
                    orders.setStatus(Orders.CANCELLED);
                    orders.setCancelReason("订单超时，自动取消");
                    orders.setCancelTime(LocalDateTime.now());
                    orderMapper.update(orders);
                    log.info("[MQ-Consumer-Timeout] 订单已自动取消，orderNumber={}", orderNumber);
                } else {
                    // tradeStatus 为 null（查询失败）或 TRADE_CLOSED 或不存在
                    // 查询失败时保守处理：仍取消订单，依赖支付宝回调的可靠性
                    log.info("[MQ-Consumer-Timeout] 支付宝无有效交易记录（tradeStatus={}），取消订单，orderNumber={}", tradeStatus, orderNumber);
                    orders.setStatus(Orders.CANCELLED);
                    orders.setCancelReason("订单超时，自动取消");
                    orders.setCancelTime(LocalDateTime.now());
                    orderMapper.update(orders);
                    log.info("[MQ-Consumer-Timeout] 订单已自动取消，orderNumber={}", orderNumber);
                }
            } finally {
                distributedLockUtil.unlock(orderId, lockValue);
                log.info("[MQ-Consumer-Timeout] 释放分布式锁，orderId={}", orderId);
            }

            long cost = System.currentTimeMillis() - startTime;
            log.info("[MQ-Consumer-Timeout] 处理完成，orderNumber={}, 耗时={}ms", orderNumber, cost);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("[MQ-Consumer-Timeout] 处理订单超时消息失败，耗时={}ms, 错误={}", cost, e.getMessage(), e);
        }
    }
}
package com.jinse.mq;

import com.alibaba.fastjson.JSON;
import com.jinse.entity.OrderMessage;
import com.jinse.mapper.OrderMapper;
import com.jinse.webSocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单通知消费者
 * 监听订单创建事件，通过 WebSocket 向管理端推送新订单提醒
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "order-created-topic", consumerGroup = "order-notification-group")
public class OrderNotificationConsumer implements RocketMQListener<String> {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void onMessage(String message) {
        long startTime = System.currentTimeMillis();
        log.info("[MQ-Consumer-Notification] 收到订单创建通知，topic=order-created-topic, 消息体={}", message);
        try {
            // 使用 OrderMessage 实体类解析消息
            OrderMessage orderMessage = JSON.parseObject(message, OrderMessage.class);
            Long orderId = orderMessage.getOrderId();
//            log.info("[MQ-Consumer-Notification] 解析消息成功，orderId={}", orderId);

            // 直接从数据库查询订单实体
            com.jinse.entity.Orders orders = orderMapper.getById(orderId);
            if (orders == null) {
                log.warn("[MQ-Consumer-Notification] 订单不存在，orderId={}", orderId);
                return;
            }

            // 直接使用 Orders 实体类序列化推送
            String notificationJson = JSON.toJSONString(orders);
//            log.info("[MQ-Consumer-Notification] 准备推送 WebSocket 通知，notification={}", notificationJson);

            webSocketServer.sendToAllClient(notificationJson);

            long cost = System.currentTimeMillis() - startTime;
            log.info("[MQ-Consumer-Notification] WebSocket 新订单通知已推送，orderNumber={}, 耗时={}ms", orders.getNumber(), cost);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("[MQ-Consumer-Notification] 处理订单通知消息失败，耗时={}ms, 错误={}", cost, e.getMessage(), e);
        }
    }
}
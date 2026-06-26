package com.jinse.mq;

import com.alibaba.fastjson.JSON;
import com.jinse.context.BaseContext;
import com.jinse.dto.OrdersSubmitDTO;
import com.jinse.entity.OrderMessage;
import com.jinse.service.OrderService;
import com.jinse.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 异步下单消费者
 * 接收 RocketMQ 异步下单消息，完成订单创建
 * 将结果存入 Redis，前端轮询获取
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "order-submit-topic", consumerGroup = "order-submit-group")
public class AsyncOrderSubmitConsumer implements RocketMQListener<String> {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String ORDER_RESULT_PREFIX = "order:submit:result:";
    private static final long RESULT_TTL = 60; // 结果保留 60 秒

    @Override
    public void onMessage(String message) {
        long startTime = System.currentTimeMillis();
        log.info("[MQ-Consumer-AsyncSubmit] 收到异步下单消息，topic=order-submit-topic, 消息体={}", message);
        try {
            // 使用 OrderMessage 实体类解析消息
            OrderMessage orderMessage = JSON.parseObject(message, OrderMessage.class);
            String correlationId = orderMessage.getCorrelationId();
            Long userId = orderMessage.getUserId();
            log.info("[MQ-Consumer-AsyncSubmit] 解析消息成功，correlationId={}, userId={}", correlationId, userId);

            // 设置用户上下文（MQ 线程没有 HTTP 上下文，需要手动设置）
            BaseContext.setCurrentId(userId);
            log.info("[MQ-Consumer-AsyncSubmit] 已设置 BaseContext，userId={}", userId);

            try {
                OrdersSubmitDTO dto = JSON.parseObject(orderMessage.getPayload(), OrdersSubmitDTO.class);
                log.info("[MQ-Consumer-AsyncSubmit] 准备调用订单服务，correlationId={}, dto={}", correlationId, JSON.toJSONString(dto));

                OrderSubmitVO result = orderService.submit(dto);
                // 成功：写入 Redis 供前端轮询
                String resultJson = JSON.toJSONString(result);
                String redisKey = ORDER_RESULT_PREFIX + correlationId;
                redisTemplate.opsForValue().set(redisKey, resultJson, RESULT_TTL, TimeUnit.SECONDS);

                long cost = System.currentTimeMillis() - startTime;
                log.info("[MQ-Consumer-AsyncSubmit] 异步下单成功，correlationId={}, 订单号={}, Redis key={}, 耗时={}ms",
                        correlationId, result.getOrderNumber(), redisKey, cost);
            } catch (Exception e) {
                // 失败：写入错误信息
                String errorJson = "{\"error\":\"" + e.getMessage() + "\"}";
                String redisKey = ORDER_RESULT_PREFIX + correlationId;
                redisTemplate.opsForValue().set(redisKey, errorJson, RESULT_TTL, TimeUnit.SECONDS);

                long cost = System.currentTimeMillis() - startTime;
                log.error("[MQ-Consumer-AsyncSubmit] 异步下单失败，correlationId={}, Redis key={}, 耗时={}ms, 错误={}",
                        correlationId, redisKey, cost, e.getMessage(), e);
            } finally {
                // 清理线程上下文，防止内存泄漏
                BaseContext.removeCurrentId();
                log.info("[MQ-Consumer-AsyncSubmit] 已清理 BaseContext，correlationId={}", correlationId);
            }
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("[MQ-Consumer-AsyncSubmit] 解析异步下单消息失败，耗时={}ms, 错误={}", cost, e.getMessage(), e);
        }
    }
}
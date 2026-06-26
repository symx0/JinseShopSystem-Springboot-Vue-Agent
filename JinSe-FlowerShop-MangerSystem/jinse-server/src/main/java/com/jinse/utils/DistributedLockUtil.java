package com.jinse.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁工具类
 * 使用 SETNX + Lua 脚本保证加锁/解锁的原子性
 * 解决支付回调与超时关单的并发问题
 */
@Slf4j
@Component
public class DistributedLockUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "lock:order:";
    private static final long DEFAULT_LOCK_TTL = 30; // 默认锁过期时间 30 秒

    // 解锁 Lua 脚本：只有持有锁的线程才能释放锁
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    /**
     * 尝试获取锁
     * @param orderId 订单 ID
     * @return lockValue 锁的唯一标识，用于解锁；null 表示获取失败
     */
    public String tryLock(Long orderId) {
        return tryLock(orderId, DEFAULT_LOCK_TTL);
    }

    /**
     * 尝试获取锁（指定过期时间）
     */
    public String tryLock(Long orderId, long ttlSeconds) {
        String lockKey = LOCK_PREFIX + orderId;
        String lockValue = UUID.randomUUID().toString();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, ttlSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取分布式锁成功，orderId={}, lockValue={}", orderId, lockValue);
            return lockValue;
        }
        log.debug("获取分布式锁失败，orderId={}", orderId);
        return null;
    }

    /**
     * 释放锁
     * @param orderId 订单 ID
     * @param lockValue 锁的唯一标识（tryLock 返回值）
     */
    public void unlock(Long orderId, String lockValue) {
        if (lockValue == null) return;
        String lockKey = LOCK_PREFIX + orderId;
        stringRedisTemplate.execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class),
                java.util.Collections.singletonList(lockKey),
                lockValue
        );
        log.debug("释放分布式锁，orderId={}", orderId);
    }
}
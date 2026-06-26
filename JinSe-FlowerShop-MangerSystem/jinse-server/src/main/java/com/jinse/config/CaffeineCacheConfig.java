package com.jinse.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jinse.entity.Flower;
import com.jinse.mapper.FlowerMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存配置
 * 替代 Redis 读缓存，减少网络 IO，提高热点数据访问性能
 */
@Configuration
public class CaffeineCacheConfig {

    /**
     * 店铺配置缓存（状态、支付模式、费用配置、发货天数等）
     * 写操作时同步更新，读操作直接走本地内存
     */
    @Bean
    public Cache<String, Object> shopConfigCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(50)
                .build();
    }

    /**
     * 鲜花详情缓存（按 ID 查询）
     * 过期后自动从数据库加载，异步刷新避免缓存击穿
     */
    @Bean
    public Cache<Long, Flower> flowerCache(FlowerMapper flowerMapper) {
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(500)
                .build();
    }
}
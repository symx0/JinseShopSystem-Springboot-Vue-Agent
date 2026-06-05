package com.jinse.config;

import com.jinse.constant.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShopStatusInitializer implements CommandLineRunner {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void run(String... args) {
        // 启动时初始化店铺状态为停业
        Integer currentStatus = (Integer) redisTemplate.opsForValue().get(RedisConstants.KEY);
        if (currentStatus == null) {
            redisTemplate.opsForValue().set(RedisConstants.KEY, 0);
            log.info("初始化店铺状态为：停业中");
        } else {
            log.info("店铺状态已存在，当前状态：{}", currentStatus == 1 ? "营业中" : "打烊中");
        }

        // 初始化发货天数，默认1天
        Integer deliveryDays = (Integer) redisTemplate.opsForValue().get(RedisConstants.DELIVERY_DAYS_KEY);
        if (deliveryDays == null) {
            redisTemplate.opsForValue().set(RedisConstants.DELIVERY_DAYS_KEY, 1);
            log.info("初始化发货天数为：1天");
        } else {
            log.info("发货天数已存在，当前：{}天", deliveryDays);
        }

        // 初始化支付宝沙箱配置（如果不存在）
        if (redisTemplate.opsForValue().get(RedisConstants.ALIPAY_APP_ID) == null) {
            redisTemplate.opsForValue().set(RedisConstants.ALIPAY_APP_ID, "9021000157678386");
            log.info("初始化支付宝AppID");
        }
        if (redisTemplate.opsForValue().get(RedisConstants.ALIPAY_NOTIFY_URL) == null) {
            redisTemplate.opsForValue().set(RedisConstants.ALIPAY_NOTIFY_URL, "http://localhost:8080/user/order/payment/notify");
            log.info("初始化支付宝回调URL");
        }
        if (redisTemplate.opsForValue().get(RedisConstants.ALIPAY_RETURN_URL) == null) {
            redisTemplate.opsForValue().set(RedisConstants.ALIPAY_RETURN_URL, "http://localhost:5173/order");
            log.info("初始化支付宝返回URL");
        }
    }
}
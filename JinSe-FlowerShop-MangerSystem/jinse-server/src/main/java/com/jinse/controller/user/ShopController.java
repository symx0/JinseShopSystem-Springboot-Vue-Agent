package com.jinse.controller.user;


import com.github.benmanes.caffeine.cache.Cache;
import com.jinse.constant.RedisConstants;
import com.jinse.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("user/shop")
@Slf4j
@Api(tags= "用户店铺状态查询")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    @Qualifier("shopConfigCache")
    private Cache<String, Object> shopConfigCache;

    /**
     * 获取店铺状态
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus() {
        Integer status = getCachedOrRedis(RedisConstants.KEY, 0);
        log.info("获取店铺状态为：{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }

    @GetMapping("/paymentMode")
    @ApiOperation("获取支付模式")
    public Result<Integer> getPaymentMode() {
        Integer mode = getCachedOrRedis(RedisConstants.PAYMENT_MODE_KEY, 1);
        return Result.success(mode);
    }

    @GetMapping("/fee")
    @ApiOperation("获取费用配置")
    public Result<java.util.Map<String, Object>> getFee() {
        java.util.Map<String, Object> feeConfig = new java.util.HashMap<>();
        feeConfig.put("deliveryFee", getCachedOrRedis(RedisConstants.DELIVERY_FEE_KEY, 10));
        feeConfig.put("deliveryFreeThreshold", getCachedOrRedis(RedisConstants.DELIVERY_FREE_THRESHOLD_KEY, 199));
        feeConfig.put("packFee", getCachedOrRedis(RedisConstants.PACK_FEE_KEY, 0));
        return Result.success(feeConfig);
    }

    private Integer getCachedOrRedis(String key, Integer defaultValue) {
        Integer val = (Integer) shopConfigCache.getIfPresent(key);
        if (val == null) {
            val = (Integer) redisTemplate.opsForValue().get(key);
            if (val != null) shopConfigCache.put(key, val);
        }
        return val != null ? val : defaultValue;
    }
}
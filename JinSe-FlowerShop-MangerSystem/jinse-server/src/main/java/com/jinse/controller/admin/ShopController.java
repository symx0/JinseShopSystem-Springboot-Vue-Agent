package com.jinse.controller.admin;


import com.jinse.constant.RedisConstants;
import com.jinse.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")//给当前类的bean起别名，和user的ShopController区分开来
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags= "店铺管理")
public class ShopController {
    @Autowired
     private RedisTemplate redisTemplate;

    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺状态为：{}",status == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(RedisConstants.KEY,status);
        return Result.success();
    }

    /**
     * 获取店铺状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(RedisConstants.KEY);
        log.info("获取店铺状态为：{}",status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }

    @PutMapping("/deliveryDays")
    @ApiOperation("设置发货天数")
    public Result setDeliveryDays(@RequestParam Integer days){
        log.info("设置发货天数为：{}", days);
        redisTemplate.opsForValue().set(RedisConstants.DELIVERY_DAYS_KEY, days);
        return Result.success();
    }

    @GetMapping("/deliveryDays")
    @ApiOperation("获取发货天数")
    public Result<Integer> getDeliveryDays(){
        Integer days = (Integer) redisTemplate.opsForValue().get(RedisConstants.DELIVERY_DAYS_KEY);
        log.info("获取发货天数：{}天", days);
        return Result.success(days == null ? 1 : days);
    }

    // ==================== 支付宝配置 ====================

    @GetMapping("/alipay/config")
    @ApiOperation("获取支付宝配置")
    public Result getAlipayConfig(){
        java.util.Map<String, Object> config = new java.util.HashMap<>();
        config.put("appId", redisTemplate.opsForValue().get(RedisConstants.ALIPAY_APP_ID));
        config.put("privateKey", redisTemplate.opsForValue().get(RedisConstants.ALIPAY_PRIVATE_KEY));
        config.put("alipayPublicKey", redisTemplate.opsForValue().get(RedisConstants.ALIPAY_PUBLIC_KEY));
        config.put("notifyUrl", redisTemplate.opsForValue().get(RedisConstants.ALIPAY_NOTIFY_URL));
        config.put("returnUrl", redisTemplate.opsForValue().get(RedisConstants.ALIPAY_RETURN_URL));
        config.put("subjectPrefix", redisTemplate.opsForValue().get(RedisConstants.ALIPAY_SUBJECT_PREFIX));
        return Result.success(config);
    }

    @PutMapping("/alipay/config")
    @ApiOperation("保存支付宝配置")
    public Result saveAlipayConfig(@RequestBody java.util.Map<String, String> config){
        redisTemplate.opsForValue().set(RedisConstants.ALIPAY_APP_ID, config.get("appId"));
        redisTemplate.opsForValue().set(RedisConstants.ALIPAY_PRIVATE_KEY, config.get("privateKey"));
        redisTemplate.opsForValue().set(RedisConstants.ALIPAY_PUBLIC_KEY, config.get("alipayPublicKey"));
        redisTemplate.opsForValue().set(RedisConstants.ALIPAY_NOTIFY_URL, config.get("notifyUrl"));
        redisTemplate.opsForValue().set(RedisConstants.ALIPAY_RETURN_URL, config.get("returnUrl"));
        if (config.containsKey("subjectPrefix")) {
            redisTemplate.opsForValue().set(RedisConstants.ALIPAY_SUBJECT_PREFIX, config.get("subjectPrefix"));
        }
        log.info("保存支付宝配置");
        return Result.success();
    }

    @PutMapping("/paymentMode/{mode}")
    @ApiOperation("设置支付模式：1=支付宝支付 0=模拟支付")
    public Result setPaymentMode(@PathVariable Integer mode){
        log.info("设置支付模式为：{}", mode == 1 ? "支付宝支付" : "模拟支付");
        redisTemplate.opsForValue().set(RedisConstants.PAYMENT_MODE_KEY, mode);
        return Result.success();
    }

    @GetMapping("/paymentMode")
    @ApiOperation("获取支付模式：1=支付宝支付 0=模拟支付")
    public Result<Integer> getPaymentMode(){
        Integer mode = (Integer) redisTemplate.opsForValue().get(RedisConstants.PAYMENT_MODE_KEY);
        log.info("获取支付模式：{}", mode == null ? "未设置(默认支付宝)" : (mode == 1 ? "支付宝支付" : "模拟支付"));
        return Result.success(mode == null ? 1 : mode);
    }

    @PutMapping("/fee")
    @ApiOperation("设置费用配置（配送费、免运费阈值、打包费）")
    public Result setFee(@RequestBody java.util.Map<String, Object> feeConfig){
        if (feeConfig.containsKey("deliveryFee")) {
            redisTemplate.opsForValue().set(RedisConstants.DELIVERY_FEE_KEY, feeConfig.get("deliveryFee"));
        }
        if (feeConfig.containsKey("deliveryFreeThreshold")) {
            redisTemplate.opsForValue().set(RedisConstants.DELIVERY_FREE_THRESHOLD_KEY, feeConfig.get("deliveryFreeThreshold"));
        }
        if (feeConfig.containsKey("packFee")) {
            redisTemplate.opsForValue().set(RedisConstants.PACK_FEE_KEY, feeConfig.get("packFee"));
        }
        log.info("设置费用配置：{}", feeConfig);
        return Result.success();
    }

    @GetMapping("/fee")
    @ApiOperation("获取费用配置（配送费、免运费阈值、打包费）")
    public Result<java.util.Map<String, Object>> getFee(){
        java.util.Map<String, Object> feeConfig = new java.util.HashMap<>();
        Object deliveryFee = redisTemplate.opsForValue().get(RedisConstants.DELIVERY_FEE_KEY);
        Object deliveryFreeThreshold = redisTemplate.opsForValue().get(RedisConstants.DELIVERY_FREE_THRESHOLD_KEY);
        Object packFee = redisTemplate.opsForValue().get(RedisConstants.PACK_FEE_KEY);
        feeConfig.put("deliveryFee", deliveryFee != null ? deliveryFee : 10);
        feeConfig.put("deliveryFreeThreshold", deliveryFreeThreshold != null ? deliveryFreeThreshold : 199);
        feeConfig.put("packFee", packFee != null ? packFee : 0);
        return Result.success(feeConfig);
    }
}

package com.jinse.controller.user;


import com.jinse.constant.RedisConstants;
import com.jinse.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController") //给当前类的bean起别名，和admin的ShopController区分开来
@RequestMapping("user/shop")
@Slf4j
@Api(tags= "用户店铺状态查询")
public class ShopController {
    @Autowired
     private RedisTemplate redisTemplate;

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

    @GetMapping("/paymentMode")
    @ApiOperation("获取支付模式")
    public Result<Integer> getPaymentMode(){
        Integer mode = (Integer) redisTemplate.opsForValue().get(RedisConstants.PAYMENT_MODE_KEY);
        return Result.success(mode == null ? 1 : mode);
    }

    @GetMapping("/fee")
    @ApiOperation("获取费用配置")
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

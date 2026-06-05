package com.jinse.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.jinse.constant.MessageConstant;
import com.jinse.constant.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AlipayConfigProvider {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${sky.alipay.biz-content-template}")
    private String bizContentTemplate;

    private static final String SERVER_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    public AlipayClient getAlipayClient() {
        String appId = (String) redisTemplate.opsForValue().get(RedisConstants.ALIPAY_APP_ID);
        String privateKey = (String) redisTemplate.opsForValue().get(RedisConstants.ALIPAY_PRIVATE_KEY);
        String alipayPublicKey = (String) redisTemplate.opsForValue().get(RedisConstants.ALIPAY_PUBLIC_KEY);

        if (appId == null || privateKey == null || alipayPublicKey == null
                || privateKey.contains("请填写") || alipayPublicKey.contains("请填写")) {
            throw new RuntimeException(MessageConstant.ALIPAY_NOT_CONFIGURED);
        }

        return new DefaultAlipayClient(
                SERVER_URL,
                appId,
                privateKey,
                "json",
                "UTF-8",
                alipayPublicKey,
                "RSA2"
        );
    }

    public String getNotifyUrl() {
        String url = (String) redisTemplate.opsForValue().get(RedisConstants.ALIPAY_NOTIFY_URL);
        return url != null ? url : "http://localhost:8080/user/order/payment/notify";
    }

    public String getReturnUrl() {
        String url = (String) redisTemplate.opsForValue().get(RedisConstants.ALIPAY_RETURN_URL);
        return url != null ? url : "http://localhost:3001/order";
    }

    /**
     * 支付宝同步返回地址（支付完成后浏览器跳转到后端接口，后端更新订单后重定向到前端）
     */
    public String getSyncReturnUrl() {
        String notifyUrl = getNotifyUrl();
        // 从notify_url推导出return_url：替换路径为/payment/return
        if (notifyUrl != null && notifyUrl.contains("/payment/notify")) {
            return notifyUrl.replace("/payment/notify", "/payment/return");
        }
        return "http://localhost:8080/user/order/payment/return";
    }

    public String getBizContentTemplate() {
        return bizContentTemplate;
    }

    public String getSubjectPrefix() {
        String prefix = (String) redisTemplate.opsForValue().get(RedisConstants.ALIPAY_SUBJECT_PREFIX);
        return prefix != null ? prefix : "锦瑟花店订单-";
    }
}

package com.jinse.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.jinse.config.AlipayConfigProvider;
import com.jinse.entity.Orders;
import com.jinse.exception.OrderBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlipayService {

    private final AlipayConfigProvider alipayConfigProvider;

    /**
     * 调用支付宝沙箱支付，返回HTML表单字符串
     */
    public String pay(Orders orders) {
        try {
            AlipayClient alipayClient = alipayConfigProvider.getAlipayClient();
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            // return_url: 支付完成后浏览器跳转回后端同步返回接口，后端更新订单后重定向到前端订单页
            request.setReturnUrl(alipayConfigProvider.getSyncReturnUrl());
            // notify_url: 支付完成后支付宝服务器异步通知
            request.setNotifyUrl(alipayConfigProvider.getNotifyUrl());

            String bizContent = String.format(
                    alipayConfigProvider.getBizContentTemplate(),
                    orders.getNumber(),
                    orders.getAmount().toString(),
                    alipayConfigProvider.getSubjectPrefix() + orders.getNumber()
            );
            request.setBizContent(bizContent);

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "POST");
            if (response.isSuccess()) {
                log.info("支付宝沙箱支付调用成功，订单号：{}", orders.getNumber());
                return response.getBody();
            } else {
                log.error("支付宝沙箱支付调用失败，订单号：{}，错误：{}", orders.getNumber(), response.getMsg());
                throw new OrderBusinessException("支付调用失败");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝沙箱支付异常", e);
            throw new OrderBusinessException("支付异常：" + e.getMessage());
        }
    }

    /**
     * 支付宝退款
     */
    public boolean refund(Orders orders, String reason) {
        try {
            AlipayClient alipayClient = alipayConfigProvider.getAlipayClient();
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

            String bizContent = String.format(
                    "{\"out_trade_no\":\"%s\",\"refund_amount\":\"%s\",\"refund_reason\":\"%s\"}",
                    orders.getNumber(),
                    orders.getAmount().toString(),
                    reason != null ? reason : "订单取消退款"
            );
            request.setBizContent(bizContent);

            AlipayTradeRefundResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("支付宝退款成功，订单号：{}，退款金额：{}", orders.getNumber(), orders.getAmount());
                return true;
            } else {
                log.error("支付宝退款失败，订单号：{}，错误：{}，子错误：{}", orders.getNumber(), response.getMsg(), response.getSubMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝退款异常", e);
            return false;
        }
    }

    /**
     * 主动查询支付宝交易状态
     * @param outTradeNo 商户订单号
     * @return 交易状态（TRADE_SUCCESS/TRADE_FINISHED/TRADE_CLOSED/WAIT_BUYER_PAY 等），查询失败返回 null
     */
    public String queryTradeStatus(String outTradeNo) {
        try {
            AlipayClient alipayClient = alipayConfigProvider.getAlipayClient();
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent(String.format("{\"out_trade_no\":\"%s\"}", outTradeNo));
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                log.info("支付宝交易查询成功，订单号：{}，交易状态：{}", outTradeNo, response.getTradeStatus());
                return response.getTradeStatus();
            } else {
                log.warn("支付宝交易查询失败，订单号：{}，错误：{}，子错误：{}", outTradeNo, response.getMsg(), response.getSubMsg());
                return null;
            }
        } catch (AlipayApiException e) {
            log.error("支付宝交易查询异常，订单号：{}", outTradeNo, e);
            return null;
        }
    }
}

package com.jinse.controller.user;


import cn.hutool.db.sql.Order;
import com.alibaba.fastjson.JSON;
import com.jinse.context.BaseContext;
import com.jinse.dto.OrdersPaymentDTO;
import com.jinse.dto.OrdersSubmitDTO;
import com.jinse.entity.OrderDetail;
import com.jinse.entity.Orders;
import com.jinse.config.AlipayConfigProvider;
import com.jinse.annotation.RateLimit;
import com.jinse.utils.DistributedLockUtil;
import com.jinse.result.Result;
import com.jinse.service.AlipayService;
import com.jinse.service.OrderService;
import com.jinse.vo.OrderDetailVO;
import com.jinse.vo.OrderPaymentVO;
import com.jinse.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

@RestController("userOrderController")
@Api(tags = "用户端相关接口")
@RequestMapping("/user/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private AlipayConfigProvider alipayConfigProvider;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DistributedLockUtil distributedLockUtil;

    /**
     * 订单提交（异步下单）
     * 通过 RocketMQ 异步处理，立即返回 correlationId
     * 前端通过轮询查询接口获取下单结果
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("订单提交（异步）")
    @RateLimit(window = 1, maxCount = 5, message = "下单过于频繁，请稍后再试")
    public Result submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        Long userId = BaseContext.getCurrentId();
        String correlationId = orderService.submitAsync(ordersSubmitDTO, userId);
        // 返回 correlationId，前端通过轮询查询接口获取结果
        return Result.success(correlationId);
    }

    /**
     * 查询异步下单结果
     * @param correlationId 关联ID
     * @return 下单结果或 "processing" 表示处理中
     */
    @GetMapping("/submit/result/{correlationId}")
    @ApiOperation("查询异步下单结果")
    public Result getSubmitResult(@PathVariable String correlationId){
        String key = "order:submit:result:" + correlationId;
        String result = (String) redisTemplate.opsForValue().get(key);
        if (result == null) {
            return Result.success("processing");
        }
        // 解析 JSON 字符串为对象返回给前端
        try {
            Object parsed = JSON.parse(result);
            return Result.success(parsed);
        } catch (Exception e) {
            return Result.success(result);
        }
    }

    /**
     * 订单查询
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("订单查询")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "Bearer Token",
                    required = true,
                    paramType = "header"
            )
    })
    public Result<List<Orders>> listOrder(){
        Long userId= BaseContext.getCurrentId();
        List<Orders> orderList=orderService.list(userId);
        return Result.success(orderList);
    }

    /**
     * 查看订单详情
     * @param orderId
     * @return
     */
    @GetMapping("/detail/{orderId}")
    @ApiOperation("查看订单详情")
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "token",
                    value = "Bearer Token",
                    required = true,
                    paramType = "header"
            )
    })
    public Result<List<OrderDetailVO>> listOrderDetail(@PathVariable  Long orderId){
        List<OrderDetailVO> orderDetailVOList=orderService.listOrderDetail(orderId);
//        log.info("订单详情查询:{}",orderDetailVOList);
        return Result.success(orderDetailVOList);
    }

    /**
     * 确认收货
     * @param orderId
     * @return
     */
    @PutMapping("/receipt/{orderId}")
    @ApiOperation("确认收货")
    public Result receipt(@PathVariable Long orderId){
        log.info("确认收货，订单id：{}", orderId);
        Long userId = BaseContext.getCurrentId();
        orderService.receipt(orderId, userId);
        return Result.success();
    }

    /**
     * 模拟支付（绕开支付宝，直接将订单状态改为已付款，用于测试）
     */
    @PutMapping("/mock-payment/{orderId}")
    @ApiOperation("模拟支付（测试用）")
    public Result mockPayment(@PathVariable Long orderId){
        log.info("模拟支付，订单id：{}", orderId);
        Long userId = BaseContext.getCurrentId();
        orderService.payment(orderId, userId);
        return Result.success();
    }

    /**
     * 支付宝沙箱支付页面（直接返回HTML，浏览器可直接导航到此地址）
     */
    @GetMapping(value = "/payment/page/{orderId}", produces = "text/html;charset=UTF-8")
    @ApiOperation("订单支付页面")
    @ResponseBody
    public String paymentPage(@PathVariable Long orderId){
        log.info("订单支付页面，订单id：{}", orderId);
        Orders orders = orderService.getById(orderId);
        if (orders == null) {
            return "<html><body><h3>订单不存在</h3></body></html>";
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            return "<html><body><h3>订单状态不正确，无法付款</h3></body></html>";
        }
        String form = alipayService.pay(orders);
        return form;
    }

    /**
     * 用户取消订单（未付款直接取消，已付款走退货申请流程）
     * @param orderId
     * @return
     */
    @PutMapping("/cancel/{orderId}")
    @ApiOperation("用户取消订单")
    public Result cancel(@PathVariable Long orderId){
        log.info("用户取消订单，订单id：{}", orderId);
        Long userId = BaseContext.getCurrentId();
        orderService.userCancel(orderId, userId);
        return Result.success();
    }

    /**
     * 申请退货
     * @param orderId
     * @return
     */
    @PutMapping("/refund/{orderId}")
    @ApiOperation("申请退货")
    public Result refund(@PathVariable Long orderId){
        log.info("申请退货，订单id：{}", orderId);
        Long userId = BaseContext.getCurrentId();
        orderService.refund(orderId, userId);
        return Result.success();
    }



    /**
     * 支付宝支付回调通知（异步通知）
     */
    @PostMapping(value = "/payment/notify", produces = "text/plain;charset=UTF-8")
    @ApiOperation("支付宝支付回调")
    public String paymentNotify(HttpServletRequest request) {
        log.info("========== 支付宝支付回调通知 ==========");
        log.info("请求方法：{}", request.getMethod());
        log.info("请求URI：{}", request.getRequestURI());
        
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        log.info("回调参数：{}", params);

        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");
        
        log.info("交易状态：{}，商户订单号：{}", tradeStatus, outTradeNo);

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            Orders orders = orderService.getByNumber(outTradeNo);
            if (orders == null) {
                log.error("订单不存在，订单号：{}", outTradeNo);
                return "success";
            }
            log.info("订单状态：{}，期望状态：{}", orders.getStatus(), Orders.PENDING_PAYMENT);

            if (Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
                // 获取分布式锁，防止与超时关单并发
                // 重试3次，每次间隔1秒，确保不会因超时消费者持锁而丢失支付结果
                String lockValue = null;
                for (int i = 0; i < 3; i++) {
                    lockValue = distributedLockUtil.tryLock(orders.getId(), 15);
                    if (lockValue != null) break;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                if (lockValue == null) {
                    log.warn("支付回调获取分布式锁超时，订单 {}，支付宝会重试回调", orders.getNumber());
                    return "failure";
                }
                try {
                    // 再次检查状态，防止获取锁期间状态已变更
                    Orders freshOrder = orderService.getById(orders.getId());
                    if (freshOrder != null && Orders.PENDING_PAYMENT.equals(freshOrder.getStatus())) {
                        orderService.payment(orders.getId(), orders.getUserId());
                        log.info("订单支付成功，订单号：{}，订单ID：{}", outTradeNo, orders.getId());
                    } else {
                        log.warn("订单状态已变更，订单号：{}，当前状态：{}", outTradeNo,
                                freshOrder != null ? freshOrder.getStatus() : "null");
                    }
                } finally {
                    distributedLockUtil.unlock(orders.getId(), lockValue);
                }
            } else {
                log.warn("订单状态不是待付款，无法更新，订单号：{}，当前状态：{}", outTradeNo, orders.getStatus());
            }
        } else {
            log.warn("交易状态不是成功状态：{}", tradeStatus);
        }
        return "success";
    }

    /**
     * 主动查询支付状态（前端从支付宝返回后调用，补偿异步回调可能丢失的情况）
     */
    @GetMapping("/payment/status/{orderId}")
    @ApiOperation("查询支付状态")
    public Result checkPaymentStatus(@PathVariable Long orderId) {
        log.info("查询支付状态，订单id：{}", orderId);
        Orders orders = orderService.getById(orderId);
        if (orders == null) {
            return Result.error("订单不存在");
        }
        // 如果订单已不是待付款状态，直接返回当前状态
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            return Result.success(orders.getStatus());
        }
        // 主动查询支付宝交易状态
        String tradeStatus = alipayService.queryTradeStatus(orders.getNumber());
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 获取分布式锁，防止与超时关单并发
            String lockValue = distributedLockUtil.tryLock(orders.getId(), 15);
            if (lockValue == null) {
                log.info("订单 {} 正在被超时关单处理，跳过补偿支付", orders.getNumber());
                return Result.success(orders.getStatus());
            }
            try {
                // 再次检查状态
                Orders freshOrder = orderService.getById(orders.getId());
                if (freshOrder != null && Orders.PENDING_PAYMENT.equals(freshOrder.getStatus())) {
                    orderService.payment(orders.getId(), orders.getUserId());
                    log.info("补偿更新订单支付成功，订单号：{}", orders.getNumber());
                    return Result.success(Orders.TO_BE_CONFIRMED);
                } else {
                    log.warn("订单状态已变更，订单号：{}，当前状态：{}", orders.getNumber(),
                            freshOrder != null ? freshOrder.getStatus() : "null");
                    return Result.success(freshOrder != null ? freshOrder.getStatus() : null);
                }
            } finally {
                distributedLockUtil.unlock(orders.getId(), lockValue);
            }
        }
        // 仍未支付
        return Result.success(orders.getStatus());
    }

    /**
     * 支付宝支付同步返回（用户支付完成后跳转回来）
     * 当异步通知不可达时，通过此接口更新订单状态
     */
    @GetMapping(value = "/payment/return", produces = "text/html;charset=UTF-8")
    @ApiOperation("支付宝支付同步返回")
    public String paymentReturn(HttpServletRequest request) {
        log.info("========== 支付宝支付同步返回 ==========");
        
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        log.info("同步返回参数：{}", params);

        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo != null) {
            Orders orders = orderService.getByNumber(outTradeNo);
            if (orders != null && Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
                orderService.payment(orders.getId(), orders.getUserId());
                log.info("同步返回更新订单支付成功，订单号：{}", outTradeNo);
            }
        }
        // 重定向到前端订单页面
        return "<html><body><h3>支付成功，正在返回订单页面...</h3><script>setTimeout(function(){window.location.href='http://localhost:3001/order';},1500);</script></body></html>";
    }
}

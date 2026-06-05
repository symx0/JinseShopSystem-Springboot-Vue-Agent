package com.jinse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {

    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已确认 6已完成 7已取消 8退货申请中
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_CONFIRMED = 2;
    public static final Integer CONFIRMED = 3;
    public static final Integer DELIVERY_IN_PROGRESS = 4;
    public static final Integer RECEIPT_CONFIRMED = 5;
    public static final Integer COMPLETED = 6;
    public static final Integer CANCELLED = 7;
    public static final Integer RETURN_REQUESTED = 8;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;
    // 订单ID
    private Long id;
    //订单号
    private String number;
    //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
    private Integer status;
    //下单用户id
    private Long userId;
    //地址id
    private Long addressBookId;
    //下单时间
    private LocalDateTime orderTime;
    //结账时间
    private LocalDateTime checkoutTime;
    //支付方式 1微信，2支付宝
    private Integer payMethod;
    //支付状态 0未支付 1已支付 2退款
    private Integer payStatus;
    //实收金额
    private BigDecimal amount;
    //备注
    private String remark;
    //用户名
    private String userName;
    //手机号
    private String phone;
    //地址
    private String address;
    //收货人
    private String consignee;
    //订单取消原因
    private String cancelReason;
    //订单拒绝原因
    private String rejectionReason;
    //退货前原始状态
    private Integer previousStatus;
    //订单取消时间
    private LocalDateTime cancelTime;
    //预计送达时间
    private LocalDateTime estimatedDeliveryTime;
    //配送状态  1立即送出  0选择具体时间
    private Integer deliveryStatus;
    //送达时间
    private LocalDateTime deliveryTime;
    //打包费
    private int packAmount;
    //配送费
    private int deliveryFee;
}

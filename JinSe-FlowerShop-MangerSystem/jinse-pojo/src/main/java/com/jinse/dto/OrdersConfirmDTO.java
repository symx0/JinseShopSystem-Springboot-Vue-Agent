package com.jinse.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersConfirmDTO implements Serializable {

    private Long id;
    /**
     * 订单状态 1待付款 2待接单 3已接单 4派送中 5已确认 6已完成 7已取消 8退货申请中
     */
    private Integer status;
    //取消原因
    private String cancelReason;

}

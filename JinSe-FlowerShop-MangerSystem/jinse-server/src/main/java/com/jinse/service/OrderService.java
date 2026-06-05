package com.jinse.service;


import com.jinse.dto.OrdersConfirmDTO;
import com.jinse.dto.OrdersPageQueryDTO;
import com.jinse.dto.OrdersSubmitDTO;
import com.jinse.entity.OrderDetail;
import com.jinse.entity.Orders;
import com.jinse.result.PageResult;
import com.jinse.vo.OrderDetailVO;
import com.jinse.vo.OrderSubmitVO;

import java.util.List;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单查询
     * @param userId
     * @return
     */
    List<Orders> list(Long userId);

    /**
     * 订单详情查询
     * @param orderId
     * @return
     */
    List<OrderDetailVO> listOrderDetail(Long orderId);

    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void delivery(OrdersConfirmDTO ordersConfirmDTO);

    void complete(OrdersConfirmDTO ordersConfirmDTO);

    void receipt(Long orderId, Long userId);

    void payment(Long orderId, Long userId);

    void refund(Long orderId, Long userId);

    void approveRefund(Long orderId);

    void rejectRefund(OrdersConfirmDTO ordersConfirmDTO);

    void cancel(OrdersConfirmDTO ordersConfirmDTO);

    void userCancel(Long orderId, Long userId);

    Orders getById(Long id);

    Orders getByNumber(String number);

    void deleteBatch(List<Long> ids);
}

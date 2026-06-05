package com.jinse.controller.admin;

import com.jinse.dto.OrdersConfirmDTO;
import com.jinse.dto.OrdersPageQueryDTO;
import com.jinse.entity.Orders;
import com.jinse.result.PageResult;
import com.jinse.result.Result;
import com.jinse.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = "订单管理")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    @ApiOperation("订单分页查询")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单分页查询：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单：{}", ordersConfirmDTO);
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/delivery")
    @ApiOperation("送出")
    public Result delivery(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("送出：{}", ordersConfirmDTO);
        orderService.delivery(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/complete")
    @ApiOperation("完成订单")
    public Result complete(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("完成订单：{}", ordersConfirmDTO);
        orderService.complete(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("取消订单：{}", ordersConfirmDTO);
        orderService.cancel(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/approve-refund/{id}")
    @ApiOperation("同意退货——取消订单并退款")
    public Result approveRefund(@PathVariable Long id) {
        log.info("同意退货，订单id：{}", id);
        orderService.approveRefund(id);
        return Result.success("已同意退货，已退款");
    }

    @PutMapping("/reject-refund")
    @ApiOperation("拒绝退货")
    public Result rejectRefund(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("拒绝退货，订单id：{}，原因：{}", ordersConfirmDTO.getId(), ordersConfirmDTO.getCancelReason());
        orderService.rejectRefund(ordersConfirmDTO);
        return Result.success("已拒绝退货申请");
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("查看订单详情")
    public Result detail(@PathVariable Long id) {
        log.info("查看订单详情：{}", id);
        Map<String, Object> result = new HashMap<>();
        Orders order = orderService.getById(id);
        result.put("order", order);
        result.put("details", orderService.listOrderDetail(id));
        return Result.success(result);
    }

    @DeleteMapping
    @ApiOperation("批量删除订单")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除订单：{}", ids);
        orderService.deleteBatch(ids);
        return Result.success("删除成功");
    }
}
package com.jinse.service.impl;

import com.alibaba.fastjson.JSON;
import com.jinse.constant.MessageConstant;
import com.jinse.constant.RedisConstants;
import com.jinse.context.BaseContext;
import com.jinse.dto.OrdersConfirmDTO;
import com.jinse.dto.OrdersPageQueryDTO;
import com.jinse.dto.OrdersSubmitDTO;
import com.jinse.entity.*;
import com.jinse.enumeration.OrderChainMarkEnum;
import com.jinse.exception.AddressBookBusinessException;
import com.jinse.exception.OrderBusinessException;
import com.jinse.framework.designPattern.designpattern.chain.AbstractChainContext;
import com.jinse.mapper.*;
import com.jinse.result.PageResult;
import com.jinse.service.ActivityService;
import com.jinse.service.AlipayService;
import com.jinse.service.OrderService;
import com.jinse.utils.RedisClient;
import com.jinse.vo.OrderDetailVO;
import com.jinse.vo.OrderSubmitVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FlowerMapper flowerMapper;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivitySaleMapper activitySaleMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private AlipayService alipayService;
    @Autowired
    private RedisClient redisClient;

    @Autowired
    private AbstractChainContext<OrdersSubmitDTO> orderSubmitAbstractChainContext;
    @Autowired
    private AbstractChainContext<OrderDetail> orderDetailAbstractChainContext;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    /**
     * 异步下单（通过 RocketMQ）
     * @param ordersSubmitDTO 订单提交数据
     * @param userId 用户ID
     * @return correlationId 用于前端通过 WebSocket 接收结果
     */
    public String submitAsync(OrdersSubmitDTO ordersSubmitDTO, Long userId) {
        // 责任链校验订单数据
        orderSubmitAbstractChainContext.handler(OrderChainMarkEnum.ORDER_SUBMIT_FILTER.name(), ordersSubmitDTO);
        
        // 发送异步下单消息到 RocketMQ
        String correlationId = java.util.UUID.randomUUID().toString();
        try {
            OrderMessage orderMessage = OrderMessage.builder()
                    .correlationId(correlationId)
                    .userId(userId)
                    .payload(JSON.toJSONString(ordersSubmitDTO))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String jsonStr = JSON.toJSONString(orderMessage);
            Message<String> message = MessageBuilder.withPayload(jsonStr).build();

            log.info("[MQ-Producer] 开始发送异步下单消息，topic=order-submit-topic, correlationId={}, userId={}", 
                    correlationId, userId);

            rocketMQTemplate.syncSend("order-submit-topic", message);

            log.info("[MQ-Producer] 异步下单消息发送成功，correlationId={}", correlationId);
        } catch (Exception e) {
            log.error("[MQ-Producer] 发送异步下单消息失败，correlationId={}, 错误={}", 
                    correlationId, e.getMessage(), e);
        }
        return correlationId;
    }

    /**
     * 订单提交
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        String lockKey = RedisConstants.ORDER_SUBMIT_LOCK_KEY + userId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", RedisConstants.ORDER_SUBMIT_LOCK_TTL, java.util.concurrent.TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            throw new OrderBusinessException(MessageConstant.OPERATION_TOO_FREQUENT);
        }
        try {
            return doSubmit(ordersSubmitDTO, userId);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private OrderSubmitVO doSubmit(OrdersSubmitDTO ordersSubmitDTO, Long userId) {
        //责任链校验订单数据
        orderSubmitAbstractChainContext.handler(OrderChainMarkEnum.ORDER_SUBMIT_FILTER.name(),ordersSubmitDTO);
        //处理各种业务异常（地址簿为空、购物车为空）
        AddressBook addressBook=addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询用户购物车记录(根据用户id)
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList==null||shoppingCartList.size()==0){
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //创建订单
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(generateOrderNumber());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT); //设置订单状态为"待付款"
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserId(userId); //判断当前订单是属于哪个用户的
        orders.setAddress(addressBook.getDetail());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        // 设置用户名
        User user = userMapper.getById(userId);

        if (user != null) {
//            log.info("下单用户信息：{}", user);
            orders.setUserName(user.getName() != null ? user.getName() : user.getUsername());
        }

        // 设置预计发货时间：如果前端未指定，则默认当前时间 + 配置的发货天数
        if (orders.getEstimatedDeliveryTime() == null) {
            Integer deliveryDays = (Integer) redisTemplate.opsForValue().get(RedisConstants.DELIVERY_DAYS_KEY);
            if (deliveryDays == null)
                deliveryDays = 1;
            orders.setEstimatedDeliveryTime(LocalDateTime.now().plusDays(deliveryDays));
        }

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList=new ArrayList<>();
        //向订单明细表插入n条数据 (一个订单对应多个订单明细)
        for(ShoppingCart cart:shoppingCartList){ //遍历购物车集合里的商品数据
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId()); //设置订单明细所关联的订单的id

            //责任链校验订单明细数据
            orderDetailAbstractChainContext.handler(OrderChainMarkEnum.ORDER_DETAIL_FILTER.name(),orderDetail);

            orderDetailList.add(orderDetail);

            // 扣减活动库存：检查该商品是否为促销商品
            deductActivityStock(cart.getFlowerId(), cart.getNumber());
        }
        //将多个订单明细批量插入
        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车（根据用户id）
        shoppingCartMapper.deleteByUserId(userId);

        // 发送 RocketMQ 消息：订单创建事件 → WebSocket 通知管理端
        sendOrderCreatedEvent(orders);
        // 发送延迟消息：15分钟后检查订单是否已付款，未付款则自动取消
        sendOrderTimeoutDelayMessage(orders);

        //封装VO返回结果
        OrderSubmitVO orderSubmitVO=OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .estimatedDeliveryTime(orders.getEstimatedDeliveryTime())
                .build();

        return orderSubmitVO;
    }


    /**
     * 订单查询
     * @param userId
     * @return
     */
    public List<Orders> list(Long userId) {
        log.info("订单查询：用户id:{}",userId);
        return orderMapper.getByUserId(userId);
    }

    /**
     * 订单明细查询
     * @param orderId
     * @return
     */
    public List<OrderDetailVO> listOrderDetail(Long orderId) {

        List<OrderDetailVO> orderDetailVOList=new ArrayList<>();
        List<OrderDetail> orderDetailList=orderDetailMapper.listOrderDetail(orderId);
        for(OrderDetail orderDetail:orderDetailList){
            OrderDetailVO orderDetailVO=new OrderDetailVO();

            Flower flower=flowerMapper.getById(orderDetail.getFlowerId());

            orderDetailVO.setId(orderDetail.getId());
            orderDetailVO.setNumber(orderDetail.getNumber());
            orderDetailVO.setAmount(orderDetail.getAmount().multiply(new java.math.BigDecimal(orderDetail.getNumber())));
            orderDetailVO.setImage(flower != null ? flower.getImage() : null);
            orderDetailVO.setFlowerName(flower != null ? flower.getName() : orderDetail.getName());

            orderDetailVOList.add(orderDetailVO);

//            log.info("orderDetailVOList:{}",orderDetailVOList);
        }
        return orderDetailVOList;
    }

    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    public void delivery(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    public void complete(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        if (!Orders.RECEIPT_CONFIRMED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_RECEIPT_CONFIRMED);
        }
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }

    public void receipt(Long orderId, Long userId) {
        Orders orders = orderMapper.getById(orderId);
        if (orders == null || !orders.getUserId().equals(userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        if (!Orders.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_DELIVERING);
        }
        orders.setStatus(Orders.RECEIPT_CONFIRMED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    public void payment(Long orderId, Long userId) {
        Orders orders = orderMapper.getById(orderId);
        if (orders == null || !orders.getUserId().equals(userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        if (!Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_CANNOT_PAY);
        }
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orders.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 用户申请退货——改为退货申请状态，需商家审核
     */
    public void refund(Long orderId, Long userId) {
        Orders orders = orderMapper.getById(orderId);
        if (orders == null || !orders.getUserId().equals(userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        // 已付款且未取消的订单都可以申请退货
        if (Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_PAID_CANNOT_REFUND);
        }
        if (Orders.CANCELLED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_ALREADY_CANCELLED);
        }
        if (Orders.RETURN_REQUESTED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.REFUND_ALREADY_APPLIED);
        }
        // 用 cancelReason 编码前状态，格式：前状态|||原因，用于拒绝时准确恢复
        orders.setPreviousStatus(orders.getStatus());
        orders.setStatus(Orders.RETURN_REQUESTED);
        orders.setCancelReason(orders.getPreviousStatus() + "|||用户申请退货");
        orders.setRejectionReason(null);
        orderMapper.update(orders);
    }

    /**
     * 商家同意退货——取消订单并退款
     */
    @Transactional
    public void approveRefund(Long orderId) {
        Orders orders = orderMapper.getById(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        if (!Orders.RETURN_REQUESTED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_REFUND_STATUS);
        }
        // 调用支付宝退款
        if (Orders.PAID.equals(orders.getPayStatus())) {
            try {
                alipayService.refund(orders, "商家同意退货");
            } catch (Exception e) {
                log.warn("支付宝退款失败，订单号：{}，错误：{}", orders.getNumber(), e.getMessage());
            }
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("商家同意退货");
        orders.setCancelTime(LocalDateTime.now());
        orders.setPayStatus(Orders.REFUND);
        orderMapper.update(orders);
        // 回增活动库存
        restoreActivityStock(orderId);
    }

    /**
     * 商家拒绝退货
     */
    public void rejectRefund(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        if (!Orders.RETURN_REQUESTED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_REFUND_STATUS);
        }
        // 从 cancelReason 中解析退货前状态（格式：前状态|||原因），bit运算兼容
        Integer restoredStatus = null;
        if (orders.getCancelReason() != null && orders.getCancelReason().contains("|||")) {
            try {
                restoredStatus = Integer.parseInt(orders.getCancelReason().split("\\|\\|\\|")[0]);
            } catch (NumberFormatException ignored) {}
        }
        orders.setStatus(restoredStatus != null ? restoredStatus : Orders.PAID.equals(orders.getPayStatus()) ? Orders.TO_BE_CONFIRMED : Orders.PENDING_PAYMENT);
        orders.setRejectionReason(ordersConfirmDTO.getCancelReason() != null ? ordersConfirmDTO.getCancelReason() : "商家拒绝退货");
        orders.setCancelReason(null);
        orders.setPreviousStatus(null);
        orderMapper.update(orders);
    }

    @Transactional
    public void cancel(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        // 已付款的订单取消时调用支付宝退款
        if (Orders.PAID.equals(orders.getPayStatus())) {
            boolean refundSuccess = false;
            try {
                refundSuccess = alipayService.refund(orders, ordersConfirmDTO.getCancelReason() != null ? ordersConfirmDTO.getCancelReason() : "管理员取消订单");
            } catch (Exception e) {
                log.warn("支付宝退款失败，订单号：{}，错误：{}", orders.getNumber(), e.getMessage());
            }
            if (refundSuccess) {
                orders.setPayStatus(Orders.REFUND);
            }
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersConfirmDTO.getCancelReason() != null ? ordersConfirmDTO.getCancelReason() : "管理员取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
        // 回增活动库存
        restoreActivityStock(ordersConfirmDTO.getId());
    }

    @Transactional
    public void userCancel(Long orderId, Long userId) {
        Orders orders = orderMapper.getById(orderId);
        if (orders == null || !orders.getUserId().equals(userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_EXIST);
        }
        if (Orders.CANCELLED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_ALREADY_CANCELLED);
        }
        if (Orders.RETURN_REQUESTED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.REFUND_ALREADY_APPLIED);
        }
        // 待付款状态：直接取消
        if (Orders.PENDING_PAYMENT.equals(orders.getStatus())) {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("用户取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
            // 回增活动库存
            restoreActivityStock(orderId);
            return;
        }
        // 已付款状态：走退货申请流程，需商家审核后才能退款
        orders.setPreviousStatus(orders.getStatus());
        orders.setStatus(Orders.RETURN_REQUESTED);
        orders.setCancelReason(orders.getPreviousStatus() + "|||用户申请取消/退货");
        orders.setRejectionReason(null);
        orderMapper.update(orders);
    }

    public Orders getById(Long id) {
        return orderMapper.getById(id);
    }

    public Orders getByNumber(String number) {
        return orderMapper.getByNumber(number);
    }

    public void deleteBatch(List<Long> ids) {
        orderMapper.deleteByIds(ids);
    }

    /**
     * 扣减活动库存（下单时调用）
     * 检查商品是否为促销商品，如果是则扣减对应的活动库存
     * @param flowerId 花束ID（flower表主键）
     * @param quantity 购买数量
     */
    private void deductActivityStock(Long flowerId, Integer quantity) {
        if (flowerId == null || quantity == null || quantity <= 0) return;
        ActivitySale activitySale = activityService.getActivitySaleByFlowerId(flowerId);
        if (activitySale == null)
            return;
        int affected = activitySaleMapper.decreaseStock(activitySale.getId(), quantity);
        if (affected == 0) {
            throw new OrderBusinessException(MessageConstant.OUT_OF_STOCK);
        }
        log.info("扣减活动库存成功，activitySaleId={}，数量={}", activitySale.getId(), quantity);
        refreshActivitySaleCache(flowerId);
    }

    /**
     * 回增活动库存（取消/退款时调用）
     * 查询订单明细中的促销商品，回增对应的活动库存
     * @param orderId 订单ID
     */
    private void restoreActivityStock(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailMapper.listOrderDetail(orderId);
        if (orderDetails == null) return;
        for (OrderDetail detail : orderDetails) {
            if (detail.getFlowerId() == null || detail.getNumber() == null || detail.getNumber() <= 0) continue;
            ActivitySale activitySale = activityService.getActivitySaleByFlowerId(detail.getFlowerId());
            if (activitySale == null) {
                log.warn("回增活动库存跳过：商品flowerId={}未找到活动促销记录，可能活动已删除", detail.getFlowerId());
                continue;
            }
            activitySaleMapper.increaseStock(activitySale.getId(), detail.getNumber());
            log.info("回增活动库存，activitySaleId={}，数量={}", activitySale.getId(), detail.getNumber());
            refreshActivitySaleCache(detail.getFlowerId());
        }
    }

    private void refreshActivitySaleCache(Long flowerId) {
        redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + flowerId);
        ActivitySale latest = activitySaleMapper.getByFlowerId(flowerId);
        if (latest != null) {
            redisClient.setWithLogicalExpire(
                    RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + flowerId,
                    latest,
                    RedisConstants.CACHE_ACTIVITY_TTL,
                    java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    private String generateOrderNumber() {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqKey = RedisConstants.ORDER_SEQ_KEY + today;
        Long seq = redisTemplate.opsForValue().increment(seqKey);
        if (seq != null && seq == 1L) {
            redisTemplate.expire(seqKey, 26, java.util.concurrent.TimeUnit.HOURS);
        }
        return today + "-" + String.format("%04d", seq);
    }

    /**
     * 发送订单创建事件（通过 RocketMQ）
     * 通知管理端有新订单
     */
    private void sendOrderCreatedEvent(Orders orders) {
        try {
            OrderMessage orderMessage = OrderMessage.builder()
                    .orderId(orders.getId())
                    .orderNumber(orders.getNumber())
                    .userId(orders.getUserId())
                    .payload(JSON.toJSONString(orders))
                    .timestamp(System.currentTimeMillis())
                    .build();

            String jsonStr = JSON.toJSONString(orderMessage);
            Message<String> message = MessageBuilder.withPayload(jsonStr).build();

//            log.info("[MQ-Producer] 开始发送订单创建事件，topic=order-created-topic, 订单号={}", orders.getNumber());
            rocketMQTemplate.syncSend("order-created-topic", message);
            log.info("[MQ-Producer] 订单创建事件发送成功，订单号={}", orders.getNumber());
        } catch (Exception e) {
            log.error("[MQ-Producer] 发送订单创建事件失败，订单号={}, 错误={}", orders.getNumber(), e.getMessage(), e);
        }
    }

    /**
     * 发送订单超时延迟消息（通过 RocketMQ）
     * 15分钟后检查订单是否已付款，未付款则自动取消
     */
    private void sendOrderTimeoutDelayMessage(Orders orders) {
        try {
            OrderMessage orderMessage = OrderMessage.builder()
                    .orderId(orders.getId())
                    .orderNumber(orders.getNumber())
                    .timestamp(System.currentTimeMillis())
                    .build();

            String jsonStr = JSON.toJSONString(orderMessage);
            Message<String> message = MessageBuilder.withPayload(jsonStr).build();

//            log.info("[MQ-Producer] 开始发送订单超时延迟消息，topic=order-timeout-topic, 订单号={}, delayLevel=15", orders.getNumber());
            rocketMQTemplate.syncSend("order-timeout-topic", message, 3000, 15);
            log.info("[MQ-Producer] 订单超时延迟消息发送成功，订单号={}", orders.getNumber());
        } catch (Exception e) {
            log.error("[MQ-Producer] 发送订单超时延迟消息失败，订单号={}, 错误={}", orders.getNumber(), e.getMessage(), e);
        }
    }

}

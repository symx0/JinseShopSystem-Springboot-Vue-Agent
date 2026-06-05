package com.jinse.mapper;

import com.jinse.dto.GoodsSalesDTO;
import com.jinse.dto.OrdersPageQueryDTO;
import com.jinse.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.Page;

@Mapper
public interface OrderMapper {


    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 查询超时的未支付订单
     * @param pendingPayment
     * @param time
     * @return
     */
    List<Orders> getByStatusAndOrderTimeLT(Integer pendingPayment, LocalDateTime time);

    /**
     * 修改订单状态
     * @param orders
     */
    void update(Orders orders);

    /**
     * 计算营业额
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据状态统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(Map map);

    /**
     * 根据用户id查询订单
     * @param userId
     * @return
     */
    @Select("select * from orders where user_id=#{userId} order by order_time desc")
    List<Orders> getByUserId(Long userId);

    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    @Select("select * from orders where number=#{number}")
    Orders getByNumber(String number);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    void deleteByIds(List<Long> ids);

    @Select("delete from orders where id=#{id}")
    void deleteById(Long id);
}

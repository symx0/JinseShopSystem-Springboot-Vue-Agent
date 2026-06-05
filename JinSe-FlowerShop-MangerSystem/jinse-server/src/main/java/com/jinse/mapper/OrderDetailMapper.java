package com.jinse.mapper;

import com.jinse.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 批量插入订单明细数据
     * @param orderDetails
     */
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 根据订单id查询订单明细数据
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id=#{orderId}")
    List<OrderDetail> listOrderDetail(Long orderId);

    /**
     * 查询用户购买某商品的累计数量（排除已取消的订单）
     * @param userId 用户ID
     * @param flowerId 花束ID
     * @return 累计购买数量
     */
    @Select("SELECT COALESCE(SUM(od.number), 0) FROM order_detail od INNER JOIN orders o ON od.order_id = o.id WHERE o.user_id = #{userId} AND od.flower_id = #{flowerId} AND o.status != 7")
    Integer sumPurchasedQuantity(@Param("userId") Long userId, @Param("flowerId") Long flowerId);

    @Select("delete from order_detail where order_id=#{orderId}")
    void deleteByOrderId(Long orderId);
}

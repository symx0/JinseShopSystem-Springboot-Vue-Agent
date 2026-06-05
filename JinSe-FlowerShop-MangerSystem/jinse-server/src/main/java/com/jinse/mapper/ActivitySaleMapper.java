package com.jinse.mapper;


import com.jinse.annotation.AutoFill;
import com.jinse.dto.ActivitySaleDTO;
import com.jinse.dto.ActivitySalePageQueryDTO;
import com.jinse.entity.Activity;
import com.jinse.entity.ActivitySale;
import com.jinse.enumeration.OperationType;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface ActivitySaleMapper {


    /**
     * 添加活动鲜花
     * @param activitySale
     */
    @AutoFill(value= OperationType.INSERT)
    void insert(ActivitySale activitySale);

    /**
     * 根据活动id和鲜花id查询活动促销鲜花
     * @param activitySaleDTO
     * @return
     */
    @Select("select * from activity_sale where activity_id=#{activityId} and flower_id=#{flowerId}")
    ActivitySale getByActivityIdAndFlowerId(ActivitySaleDTO activitySaleDTO);

    /**
     * 批量更新库存
     * @param sales
     */
    @AutoFill(value= OperationType.UPDATE)
    void batchUpdateWithVersion(List<ActivitySale> sales);

    /**
     * 根据活动id和鲜花id查询活动促销鲜花（适配库存持久化方法）
     * @param activityId
     * @param flowerId
     * @return
     */
    @Select("select * from activity_sale where activity_id=#{activityId} and flower_id=#{flowerId}")
    ActivitySale getByActivityIdAndFlowerId2(Long activityId, Long flowerId);

    /**
     * 分页查询活动促销鲜花
     * @param activitySalePageQueryDTO
     * @return
     */
    Page<ActivitySale> pageQuery(ActivitySalePageQueryDTO activitySalePageQueryDTO);


    /**
     * 根据ID查询活动中的某个鲜花
     * @param id
     */
    @Select("select * from activity_sale where id=#{id}")
    ActivitySale getById(Long id);

    /**
     * 查询活动中销量最高的鲜花图片
     * @param activityId
     * @return
     */
    @Select("SELECT f.image FROM activity_sale a_s INNER JOIN flower f ON a_s.flower_id = f.id WHERE a_s.activity_id = #{activityId} ORDER BY a_s.sale DESC LIMIT 1")
    String getBestsellerImageByActivityId(Long activityId);

    /**
     * 根据ID删除活动中的某个鲜花
     * @param id
     */
    @Delete("delete from activity_sale where id=#{id}")
    void deleteById(Long id);

    /**
     * 更新活动中的商品信息
     * @param activitySale
     */
    @AutoFill(value= OperationType.UPDATE)
    void update(ActivitySale activitySale);

    /**
     * 批量删除活动相关的促销商品
     * @param activityIds 活动ID列表
     */
    void deleteByActivityIds(List<Long> activityIds);

    /**
     * 根据促销鲜花ID（flower表中的id）查询活动促销记录
     * @param flowerId
     * @return
     */
    @Select("select * from activity_sale where flower_id=#{flowerId}")
    ActivitySale getByFlowerId(Long flowerId);

    @Select("select * from activity_sale where activity_id=#{activityId}")
    List<ActivitySale> listByActivityId(@Param("activityId") Long activityId);

    /**
     * 扣减活动库存并增加已售数量（乐观锁，防止超卖）
     * @param id activity_sale主键
     * @param quantity 扣减数量
     * @return 影响行数，0表示库存不足
     */
    @Update("update activity_sale set stock = stock - #{quantity}, sale = sale + #{quantity} where id = #{id} and stock >= #{quantity}")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 回增活动库存并减少已售数量（退款/取消时调用）
     * @param id activity_sale主键
     * @param quantity 回增数量
     */
    @Update("update activity_sale set stock = stock + #{quantity}, sale = sale - #{quantity} where id = #{id} and sale >= #{quantity}")
    void increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 统计指定活动的总销量
     * @param activityId
     * @return
     */
    @Select("SELECT COALESCE(SUM(sale), 0) FROM activity_sale WHERE activity_id = #{activityId}")
    Integer sumSaleByActivityId(@Param("activityId") Long activityId);

    /**
     * 统计指定活动的总销售额（已售数量 * 优惠价）
     * @param activityId
     * @return
     */
    @Select("SELECT COALESCE(SUM(sale * discount_price), 0) FROM activity_sale WHERE activity_id = #{activityId}")
    java.math.BigDecimal sumRevenueByActivityId(@Param("activityId") Long activityId);

    @Select("SELECT a_s.*, a.content as activityContent, a.limit_per as limitPer FROM activity_sale a_s INNER JOIN activity a ON a_s.activity_id = a.id WHERE a.status = 1")
    List<ActivitySale> listOngoingActivitySales();
}

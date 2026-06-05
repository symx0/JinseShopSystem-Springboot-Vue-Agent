package com.jinse.service;

import com.jinse.dto.*;
import com.jinse.entity.Activity;
import com.jinse.entity.ActivitySale;
import com.jinse.result.PageResult;

import java.util.List;

public interface ActivityService {


    /**
     * 活动分页查询
     * @param activityPageQueryDTO
     * @return
     */
    PageResult pageQuery(ActivityPageQueryDTO activityPageQueryDTO);

    /**
     * 活动分页查询（用户端，仅显示进行中的活动）
     * @param activityPageQueryDTO
     * @return
     */
    PageResult pageQueryForUser(ActivityPageQueryDTO activityPageQueryDTO);


    /**
     * 根据id查询活动
     * @param id
     * @return
     */
    Activity getById(Long id);

    ActivitySale getActivitySaleByFlowerId(Long flowerId);

    /**
     * 创建新活动
     * @param activityDTO
     */
    void create(ActivityDTO activityDTO);

    /**
     * 设置活动商品
     * @param activitySaleDetailDTO
     */
    void setActivitySale(ActivitySaleDetailDTO activitySaleDetailDTO);

    /**
     * 根据活动id查询活动促销商品
     * @param activitySaleDetailDTO
     * @return
     */
    PageResult pageQuerySale(ActivitySalePageQueryDTO activitySaleDetailDTO);

    /**
     * 批量删除活动
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 更新活动
     * @param activityDTO
     */
    void update(ActivityDTO activityDTO);

    /**
     * 修改活动状态
     * @param status
     * @param id
     */
    void setStatus(Integer status, Long id);

    /**
     * 批量修改活动状态
     * @param status
     * @param ids
     */
    void setStatusBatch(Integer status, List<Long> ids);

    /**
     * 根据ID删除活动中的某个商品
     * @param id
     */
    void deleteActivitySaleById(Long id, Long flowerId);

    /**
     * 更新活动中的商品信息
     * @param activitySaleDetailDTO
     */
    void updateActivitySale(ActivitySaleDetailDTO activitySaleDetailDTO);
}

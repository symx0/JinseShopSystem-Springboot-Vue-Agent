package com.jinse.mapper;


import com.jinse.annotation.AutoFill;
import com.jinse.dto.ActivityPageQueryDTO;
import com.jinse.dto.CommentPageQueryDTO;
import com.jinse.entity.Activity;
import com.jinse.entity.Comment;
import com.jinse.entity.Flower;
import com.jinse.enumeration.OperationType;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ActivityMapper {


    /**
     * 分页查询活动
     * @param activityPageQueryDTO
     * @return
     */
    Page<Activity> pageQuery(ActivityPageQueryDTO activityPageQueryDTO);

    /**
     * 分页查询活动（用户端，仅显示进行中的活动）
     * @param activityPageQueryDTO
     * @return
     */
    Page<Activity> pageQueryForUser(ActivityPageQueryDTO activityPageQueryDTO);

    /**
     * 根据id查询活动
     * @param id
     * @return
     */
    @Select("select * from activity where id=#{id}")
    Activity getById(Long id);

    /**
     * 创建活动
     * @param activity
     */
    @AutoFill(value= OperationType.INSERT)
    void insert(Activity activity);


    /**
     * 更新活动
     * @param activity
     */
    @AutoFill(value=OperationType.UPDATE)
    void update(Activity activity);

    /**
     * 批量设置活动状态
     * @param status
     * @param ids
     */
    void setStatusBatch(@org.apache.ibatis.annotations.Param("status") Integer status, @org.apache.ibatis.annotations.Param("ids") List<Long> ids);

    /**
     * 批量设置活动过期
     * @return
     */
    int batchExpireActivities();

    void deleteByIds(List<Long> ids);
}

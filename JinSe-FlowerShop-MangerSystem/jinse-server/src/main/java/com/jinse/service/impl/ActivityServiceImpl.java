package com.jinse.service.impl;


import com.jinse.constant.RedisConstants;
import com.jinse.constant.StatusConstant;
import com.jinse.dto.*;
import com.jinse.entity.Activity;
import com.jinse.entity.ActivitySale;
import com.jinse.entity.Flower;
import com.jinse.enumeration.ActivityChainMarkEnum;
import com.jinse.framework.designPattern.designpattern.chain.AbstractChainContext;
import com.jinse.mapper.ActivityMapper;
import com.jinse.mapper.ActivitySaleMapper;
import com.jinse.mapper.FlowerMapper;
import com.jinse.result.PageResult;
import com.jinse.service.ActivityService;
import com.jinse.utils.KeyGenerator;
import com.jinse.utils.RedisClient;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivitySaleMapper activitySaleMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private FlowerMapper flowerMapper;

    @Autowired
    private AbstractChainContext<ActivityDTO> activityDTOAbstractChainContext;
    @Autowired
    private AbstractChainContext<ActivitySaleDetailDTO> activitySaleDTOAbstractChainContext;


    /**
     * 活动分页查询（直接查 MySQL）
     * @param activityPageQueryDTO
     * @return
     */
    public PageResult pageQuery(ActivityPageQueryDTO activityPageQueryDTO){
        PageHelper.startPage(activityPageQueryDTO.getPage(), activityPageQueryDTO.getPageSize());
        Page<Activity> page = activityMapper.pageQuery(activityPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 活动分页查询（用户端，仅显示进行中的活动）
     * @param activityPageQueryDTO
     * @return
     */
    public PageResult pageQueryForUser(ActivityPageQueryDTO activityPageQueryDTO){
        PageHelper.startPage(activityPageQueryDTO.getPage(), activityPageQueryDTO.getPageSize());
        Page<Activity> page = activityMapper.pageQueryForUser(activityPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id查询活动（前端通过此接口将活动信息展示出去，但不是通过此接口参与活动）
     * @param id
     * @return
     */
    public Activity getById(Long id) {
        Activity activity=redisClient.queryWithMutex(
                RedisConstants.ACTIVITY_KEY,                // 缓存key前缀
                id,                                         // 活动ID
                Activity.class,                             // 缓存值类型
                activityMapper::getById,                    // 查询MySQL数据库的方法
                RedisConstants.CACHE_ACTIVITY_TTL,          // 缓存过期时间
                TimeUnit.SECONDS,                           // 缓存过期时间单位
               RedisConstants.INIT_RETRY_COUNT);            // 获取查询数据库的锁重试次数
        return activity;
    }

    @Override
    public ActivitySale getActivitySaleByFlowerId(Long flowerId) {
        return redisClient.queryWithMutex(
                RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY,
                flowerId,
                ActivitySale.class,
                activitySaleMapper::getByFlowerId,
                RedisConstants.CACHE_ACTIVITY_TTL,
                TimeUnit.SECONDS,
                RedisConstants.INIT_RETRY_COUNT
        );
    }

    /**
     * 创建活动
     * @param activityDTO
     */
    public void create(ActivityDTO activityDTO) {
        //责任链校验活动创建参数
        activityDTOAbstractChainContext.handler(ActivityChainMarkEnum.ACTIVITY_CREATE_FILTER.name(), activityDTO);
        Activity activity = new Activity();
        BeanUtils.copyProperties(activityDTO, activity);
        //默认状态为未开始
        activity.setStatus(0);

        activityMapper.insert(activity);
        Long activityId = activity.getId();

        //删除活动在缓存中的详情信息
        redisClient.deleteByPattern(RedisConstants.ACTIVITY_KEY + activityId);

    }

    /**
     * 新增活动商品
     */
    @Transactional
    public void setActivitySale(ActivitySaleDetailDTO activitySaleDetailDTO) {
        //责任链校验活动商品参数
        activitySaleDTOAbstractChainContext.handler(ActivityChainMarkEnum.ACTIVITY_SALE_CREATE_FILTER.name(), activitySaleDetailDTO);

        // 2. 直接用前端传递的鲜花信息创建促销鲜花，省去后端查询
        Flower saleFlower = Flower.builder()
                .name("[促销]" + activitySaleDetailDTO.getFlowerName())
                .categoryId(activitySaleDetailDTO.getCategoryId())
                .price(activitySaleDetailDTO.getDiscountPrice())
                .image(activitySaleDetailDTO.getFlowerImage())
                .description(activitySaleDetailDTO.getFlowerDescription())
                .status(StatusConstant.ENABLE)
                .build();
        flowerMapper.insert(saleFlower);

        // 3. activity_sale的flower_id指向新创建的促销鲜花
        ActivitySale activitySale = new ActivitySale();
        BeanUtils.copyProperties(activitySaleDetailDTO, activitySale);
        activitySale.setFlowerId(saleFlower.getId());

        activitySaleMapper.insert(activitySale);

        Long flowerId = activitySale.getFlowerId();

        if (flowerId != null) {
            redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + flowerId);
            redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + flowerId);
            redisClient.setWithLogicalExpire(
                    RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + flowerId,
                    activitySale,
                    RedisConstants.CACHE_ACTIVITY_TTL,
                    TimeUnit.SECONDS);
            redisClient.setWithLogicalExpire(
                    RedisConstants.FLOWER_KEY + flowerId,
                    saleFlower,
                    RedisConstants.CACHE_FLOWER_TTL,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * 分页查询活动商品（直接查 MySQL）
     * @param activitySalePageQueryDTO
     * @return
     */
    public PageResult pageQuerySale(ActivitySalePageQueryDTO activitySalePageQueryDTO) {
        PageHelper.startPage(activitySalePageQueryDTO.getPage(), activitySalePageQueryDTO.getPageSize());
        Page<ActivitySale> page = activitySaleMapper.pageQuery(activitySalePageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 先查询所有活动关联的促销鲜花，删除对应的flower记录
        for (Long activityId : ids) {
            List<ActivitySale> sales = activitySaleMapper.listByActivityId(activityId);
            for (ActivitySale sale : sales) {
                flowerMapper.deleteById(sale.getFlowerId());
                redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + sale.getFlowerId());
                redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + sale.getFlowerId());
            }
        }
        // 先删除活动相关的促销商品
        activitySaleMapper.deleteByActivityIds(ids);
        // 再删除活动
        activityMapper.deleteByIds(ids);
        // 删除每个活动的单个详情缓存
        for (Long id : ids) {
            redisClient.deleteByPattern(RedisConstants.ACTIVITY_KEY + id);
        }
    }

    public void update(ActivityDTO activityDTO) {
        Activity activity = new Activity();
        BeanUtils.copyProperties(activityDTO, activity);
        activityMapper.update(activity);
        Long activityId = activity.getId();
        // 删除该活动的单个详情缓存
        redisClient.deleteByPattern(RedisConstants.ACTIVITY_KEY + activityId);
    }

    public void setStatus(Integer status, Long id) {
        activityMapper.setStatusBatch(status, Collections.singletonList(id));
        // 删除该活动的单个详情缓存
        redisClient.deleteByPattern(RedisConstants.ACTIVITY_KEY + id);
    }

    public void setStatusBatch(Integer status, List<Long> ids) {
        activityMapper.setStatusBatch(status, ids);
        // 删除每个活动的单个详情缓存
        for (Long id : ids) {
            redisClient.deleteByPattern(RedisConstants.ACTIVITY_KEY + id);
        }
    }


    @Transactional
    public void deleteActivitySaleById(Long id, Long flowerId) {
        // 直接用前端传递的flowerId删除促销鲜花记录
        if (flowerId != null) {
            flowerMapper.deleteById(flowerId);
        }
        activitySaleMapper.deleteById(id);
        if (flowerId != null) {
            redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + flowerId);
            redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + flowerId);
        }

    }

    @Transactional
    public void updateActivitySale(ActivitySaleDetailDTO activitySaleDetailDTO) {
        ActivitySale activitySale = new ActivitySale();
        BeanUtils.copyProperties(activitySaleDetailDTO, activitySale);
        activitySaleMapper.update(activitySale);

        // 同步更新促销鲜花信息到flower表
        if (activitySaleDetailDTO.getFlowerId() != null) {
            // 先查询现有的鲜花信息，避免覆盖其他字段
            Flower existingFlower = flowerMapper.getById(activitySaleDetailDTO.getFlowerId());
            if (existingFlower != null) {
                Flower flower = new Flower();
                flower.setId(activitySaleDetailDTO.getFlowerId());
                
                // 只更新有变化的字段
                if (activitySaleDetailDTO.getDiscountPrice() != null) {
                    flower.setPrice(activitySaleDetailDTO.getDiscountPrice());
                }
                if (activitySaleDetailDTO.getFlowerName() != null) {
                    String currentName = existingFlower.getName();
                    String newName = activitySaleDetailDTO.getFlowerName();
                    // 检查是否已经有促销前缀，避免重复添加
                    if (!newName.startsWith("[促销]")) {
                        if (currentName != null && currentName.startsWith("[促销]")) {
                            // 保持现有的促销前缀
                            flower.setName("[促销]" + newName);
                        } else {
                            flower.setName("[促销]" + newName);
                        }
                    } else {
                        flower.setName(newName);
                    }
                }
                if (activitySaleDetailDTO.getFlowerImage() != null) {
                    flower.setImage(activitySaleDetailDTO.getFlowerImage());
                }
                if (activitySaleDetailDTO.getFlowerDescription() != null) {
                    flower.setDescription(activitySaleDetailDTO.getFlowerDescription());
                }
                if (activitySaleDetailDTO.getCategoryId() != null) {
                    flower.setCategoryId(activitySaleDetailDTO.getCategoryId());
                }
                
                flowerMapper.update(flower);
            }
        }

        if (activitySaleDetailDTO.getFlowerId() != null) {
            redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + activitySaleDetailDTO.getFlowerId());
            redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + activitySaleDetailDTO.getFlowerId());
            ActivitySale latestSale = activitySaleMapper.getByFlowerId(activitySaleDetailDTO.getFlowerId());
            if (latestSale != null) {
                redisClient.setWithLogicalExpire(
                        RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + activitySaleDetailDTO.getFlowerId(),
                        latestSale,
                        RedisConstants.CACHE_ACTIVITY_TTL,
                        TimeUnit.SECONDS);
            }
            Flower latestFlower = flowerMapper.getById(activitySaleDetailDTO.getFlowerId());
            if (latestFlower != null) {
                redisClient.setWithLogicalExpire(
                        RedisConstants.FLOWER_KEY + activitySaleDetailDTO.getFlowerId(),
                        latestFlower,
                        RedisConstants.CACHE_FLOWER_TTL,
                        TimeUnit.SECONDS);
            }
        }
    }
}

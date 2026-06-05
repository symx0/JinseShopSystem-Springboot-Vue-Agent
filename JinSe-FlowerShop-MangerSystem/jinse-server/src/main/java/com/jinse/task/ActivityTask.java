package com.jinse.task;

import cn.hutool.json.JSONUtil;
import com.jinse.constant.RedisConstants;
import com.jinse.entity.ActivitySale;
import com.jinse.mapper.ActivityMapper;
import com.jinse.mapper.ActivitySaleMapper;
import com.jinse.utils.RedisClient;
import com.jinse.utils.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Component
@Slf4j
public class ActivityTask {
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ActivitySaleMapper activitySaleMapper;
    @Autowired
    private RedisClient redisClient;

    /**
     * 每分钟确认一次是否有已结束的活动
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void autoExpireActivities() {
        log.info("开始执行活动过期处理...");
        int affected = activityMapper.batchExpireActivities();
        log.info("处理完成，更新{}条记录", affected);
    }


    /**
     * 每5分钟持久化一次活动库存到数据库（版本号实现乐观锁，防止并发覆盖）
     *
     * 1.拿到所有促销鲜花的key（即前缀为ACTIVITY_SALE_BY_FLOWER_KEY的key）
     * 2.根据这些key，从redis分别拿到每种促销鲜花的当前库存
     * 3.将每种促销鲜花的当前库存更新到数据库中
     * 4.为了保证缓存与数据库的一致性，清空缓存，等未命中缓存后自动执行缓存重建
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void persistInventory(){
        log.info("开始库存持久化...");
        //扫描Redis中所有活动库存键
        Set<String> keys = redisTemplate.keys(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + "*");
        if(keys == null || keys.isEmpty()) return;

        List<ActivitySale> updates = new ArrayList<>();

        //拿到每种促销鲜花的缓存数据
        keys.forEach(key -> {
            try {
                String json = (String) redisTemplate.opsForValue().get(key);
                RedisData redisData = JSONUtil.toBean(json, RedisData.class);

                //只有在有效期内的数据才更新到数据库
                if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
                    ActivitySale sale = JSONUtil.toBean(redisData.getData().toString(), ActivitySale.class);

                    //从数据库加载当前最新版本号（保持原子性）
                    ActivitySale dbSale = activitySaleMapper.getByActivityIdAndFlowerId2(
                            sale.getActivityId(), sale.getFlowerId());
                    if (dbSale != null) {
                        sale.setVersion(dbSale.getVersion()); //设置当前版本号
                        updates.add(sale);
                    }

                }
            } catch (Exception e) {
                log.error("解析Redis数据失败, key: {}", key, e);
            }
        });

        //批量更新数据库
        if(!updates.isEmpty()){
            activitySaleMapper.batchUpdateWithVersion(updates);
            for (ActivitySale sale : updates) {
                if (sale.getFlowerId() != null) {
                    redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + sale.getFlowerId());
                    ActivitySale latest = activitySaleMapper.getByFlowerId(sale.getFlowerId());
                    if (latest != null) {
                        redisClient.setWithLogicalExpire(
                                RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + sale.getFlowerId(),
                                latest,
                                RedisConstants.CACHE_ACTIVITY_TTL,
                                java.util.concurrent.TimeUnit.SECONDS);
                    }
                }
            }
            log.info("已更新{}条缓存记录", updates.size());
        }
    }
}

package com.jinse.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.jinse.mapper.FlowerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 布隆过滤器服务 —— 预防缓存穿透
 * 在查询鲜花前先通过布隆过滤器判断 ID 是否存在，
 * 不存在的 ID 直接返回，避免穿透到数据库
 */
@Slf4j
@Service
public class BloomFilterService {

    @Autowired
    private FlowerMapper flowerMapper;

    /**
     * 预期插入数量 10000，误判率 0.01%
     */
    private BloomFilter<Long> flowerBloomFilter;

    @PostConstruct
    public void init() {
        // 从数据库加载所有鲜花 ID 初始化布隆过滤器
        List<Long> allIds = flowerMapper.getAllIds();
        int expectedInsertions = Math.max(allIds.size(), 1000);
        flowerBloomFilter = BloomFilter.create(
                Funnels.longFunnel(),
                expectedInsertions,
                0.0001  // 0.01% 误判率
        );
        for (Long id : allIds) {
            flowerBloomFilter.put(id);
        }
        log.info("布隆过滤器初始化完成，已加载 {} 个鲜花 ID", allIds.size());
    }

    /**
     * 判断鲜花 ID 是否可能存在
     * @return false 表示一定不存在，true 表示可能存在
     */
    public boolean mightContain(Long flowerId) {
        return flowerBloomFilter.mightContain(flowerId);
    }

    /**
     * 新增鲜花时同步添加到布隆过滤器
     */
    public void add(Long flowerId) {
        flowerBloomFilter.put(flowerId);
    }

    /**
     * 全量重建布隆过滤器（定时任务或手动触发）
     */
    public void rebuild() {
        List<Long> allIds = flowerMapper.getAllIds();
        BloomFilter<Long> newFilter = BloomFilter.create(
                Funnels.longFunnel(),
                Math.max(allIds.size(), 1000),
                0.0001
        );
        for (Long id : allIds) {
            newFilter.put(id);
        }
        this.flowerBloomFilter = newFilter;
        log.info("布隆过滤器重建完成，当前 {} 个鲜花 ID", allIds.size());
    }
}
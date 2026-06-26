package com.jinse.service.impl;
import com.jinse.constant.RedisConstants;
import com.github.benmanes.caffeine.cache.Cache;
import com.jinse.enumeration.FlowerChainMarkEnum;
import com.jinse.framework.designPattern.designpattern.chain.AbstractChainContext;
import com.jinse.utils.RedisClient;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jinse.constant.MessageConstant;
import com.jinse.constant.StatusConstant;
import com.jinse.dto.FlowerDTO;
import com.jinse.dto.FlowerPageQueryDTO;
import com.jinse.entity.Flower;
import com.jinse.exception.DeletionNotAllowedException;
import com.jinse.entity.ActivitySale;
import com.jinse.mapper.ActivitySaleMapper;
import com.jinse.mapper.FlowerMapper;
import com.jinse.result.PageResult;
import com.jinse.service.FlowerService;
import com.jinse.service.BloomFilterService;
import com.jinse.vo.FlowerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlowerServiceImpl implements FlowerService {

    @Autowired
    private FlowerMapper flowerMapper;
    @Autowired
    private ActivitySaleMapper activitySaleMapper;
    @Autowired
    @Qualifier("flowerCache")
    private Cache<Long, Flower> flowerCache;

    private final AbstractChainContext<FlowerPageQueryDTO> abstractChainContext;

    @Autowired
    private BloomFilterService bloomFilterService;
    @Autowired
    private RedisClient redisClient;


    /**
     * 新增鲜花
     */
    public void save(FlowerDTO flowerDTO) {
        Flower flower = new Flower();
        BeanUtils.copyProperties(flowerDTO, flower);
        flowerMapper.insert(flower);
        // 删除该鲜花可能存在的旧缓存数据
        redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + flower.getId());
        // 清除该 ID 可能存在的空值缓存
        redisClient.deleteByPattern(RedisConstants.FLOWER_NULL_KEY + flower.getId());
        // 清除本地缓存，并添加到布隆过滤器
        flowerCache.invalidate(flower.getId());
        bloomFilterService.add(flower.getId());
    }


    /**
     * 鲜花分页查询
     */
    public PageResult pageQuery(FlowerPageQueryDTO flowerPageQueryDTO) {
        PageHelper.startPage(flowerPageQueryDTO.getPage(), flowerPageQueryDTO.getPageSize());
        Page<FlowerVO> page = flowerMapper.pageQuery(flowerPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除鲜花
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Flower flower = flowerMapper.getById(id);
            if (flower != null && flower.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            if (flower != null) {
                flowerMapper.deleteById(id);
                flowerCache.invalidate(id);
            }
        }
    }


    /**
     * 根据id查询鲜花（布隆过滤器 + 缓存空值 + Caffeine 本地缓存，防缓存穿透）
     */
    public FlowerVO getByIdWithFlavor(Long id) {
        // 第一道防线：布隆过滤器拦截不存在的 ID
        if (!bloomFilterService.mightContain(id)) {
            return new FlowerVO();
        }

        // 第二道防线：检查 Redis 缓存空值（布隆过滤器误判时生效）
        String nullKey = RedisConstants.FLOWER_NULL_KEY + id;
        if (redisClient.get(nullKey) != null) {
            log.info("命中缓存空值，flowerId={}", id);
            return new FlowerVO();
        }

        // 查询 Caffeine 本地缓存，未命中则查数据库
        Flower flower = flowerCache.get(id, key -> flowerMapper.getById(key));

        if (flower == null) {
            // 数据库也不存在 → 缓存空值到 Redis，防止布隆过滤器误判后的穿透
            redisClient.set(nullKey, "", RedisConstants.CACHE_NULL_TTL_FLOWER, java.util.concurrent.TimeUnit.MINUTES);
            log.info("鲜花不存在，已缓存空值，flowerId={}", id);
            return new FlowerVO();
        }

        FlowerVO flowerVO = new FlowerVO();
        BeanUtils.copyProperties(flower, flowerVO);
        return flowerVO;
    }

    /**
     * 根据id修改鲜花信息
     */
    @Transactional
    public void update(FlowerDTO flowerDTO) {
        ActivitySale activitySale = activitySaleMapper.getByFlowerId(flowerDTO.getId());

        Flower flower = new Flower();
        BeanUtils.copyProperties(flowerDTO, flower);
        flowerMapper.update(flower);

        if (activitySale != null) {
            ActivitySale activitySaleToUpdate = new ActivitySale();
            activitySaleToUpdate.setId(activitySale.getId());
            if (flowerDTO.getPrice() != null) {
                activitySaleToUpdate.setDiscountPrice(flowerDTO.getPrice());
            }
            activitySaleMapper.update(activitySaleToUpdate);
        }

        // 清除缓存
        flowerCache.invalidate(flowerDTO.getId());
    }

    /**
     * 批量上架/下架鲜花
     */
    @Transactional
    public void startOrStop(Integer status, List<Long> ids) {
        for (Long id : ids) {
            Flower flower = Flower.builder().id(id).status(status).build();
            flowerMapper.update(flower);
            flowerCache.invalidate(id);
        }
    }

    /**
     * 条件查询鲜花
     */
    public List<FlowerVO> query(Flower flower) {
        List<Flower> flowerList = flowerMapper.list(flower);
        List<FlowerVO> flowerVOList = new ArrayList<>();
        for (Flower f : flowerList) {
            FlowerVO flowerVO = new FlowerVO();
            BeanUtils.copyProperties(f, flowerVO);
            flowerVOList.add(flowerVO);
        }
        return flowerVOList;
    }


    /**
     * 根据分类id查询鲜花
     */
    @Override
    public List<FlowerVO> getByCategoryId(Long categoryId) {
        Flower flower = Flower.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Flower> flowerList = flowerMapper.list(flower);
        List<FlowerVO> flowerVOList = new ArrayList<>();
        for (Flower f : flowerList) {
            FlowerVO flowerVO = new FlowerVO();
            BeanUtils.copyProperties(f, flowerVO);
            flowerVOList.add(flowerVO);
        }
        return flowerVOList;
    }


    /**
     * 根据分类id查询鲜花
     */
    public List<Flower> list(Long categoryId) {
        Flower flower = Flower.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return flowerMapper.list(flower);
    }

    @Override
    public Flower getByIdFlower(Long id) {
        return flowerCache.get(id, key -> flowerMapper.getById(key));
    }

    @Override
    public List<FlowerVO> listAllEnabled() {
        Flower flower = Flower.builder()
                .status(StatusConstant.ENABLE)
                .build();
        List<Flower> flowerList = flowerMapper.list(flower);
        List<FlowerVO> flowerVOList = new ArrayList<>();
        for (Flower f : flowerList) {
            FlowerVO flowerVO = new FlowerVO();
            BeanUtils.copyProperties(f, flowerVO);
            flowerVOList.add(flowerVO);
        }
        return flowerVOList;
    }
}
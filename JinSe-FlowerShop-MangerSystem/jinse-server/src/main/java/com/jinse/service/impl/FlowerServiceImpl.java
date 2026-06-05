package com.jinse.service.impl;

import com.jinse.constant.RedisConstants;
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
import com.jinse.vo.FlowerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FlowerServiceImpl implements FlowerService {
    // 所有依赖字段统一改为final
    @Autowired
    private FlowerMapper flowerMapper;
    @Autowired
    private ActivitySaleMapper activitySaleMapper;
    @Autowired
    private RedisClient redisClient;

    private final AbstractChainContext<FlowerPageQueryDTO> abstractChainContext;




    /**
     * 新增鲜花
     * @param flowerDTO
     */
    public void save(FlowerDTO flowerDTO) {
        Flower flower=new Flower();
        BeanUtils.copyProperties(flowerDTO,flower);
        flowerMapper.insert(flower);

        //删除该鲜花可能存在的旧缓存数据
        redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + flowerDTO.getId());
    }


    /**
     * 鲜花分页查询
     * @param flowerPageQueryDTO
     * @return
     */
    public PageResult pageQuery(FlowerPageQueryDTO flowerPageQueryDTO){
        //通过责任链校验传入的flowerPageQueryDTO
        //abstractChainContext.handler(FlowerChainMarkEnum.FLOWER_QUERY_FILTER.name(),flowerPageQueryDTO);

        //PageHelper.startPage是一个对mapper层进行动态干涉的方法
        //它本身并不返回结果,而是通过干涉mapper层（拼接limit语句）,影响pageQuery方法的查询结果（只查询部分数据，减少性能开销）
        //它用于启动分页功能。它并不会直接返回分页结果，而是通过拦截 SQL 查询来实现分页，并将分页信息存储在当前线程的本地变量中（ThreadLocal）
        PageHelper.startPage(flowerPageQueryDTO.getPage(), flowerPageQueryDTO.getPageSize());
        Page<FlowerVO> page=flowerMapper.pageQuery(flowerPageQueryDTO);
        //将page对象的属性封装到pageResult对象中
        return new PageResult (page.getTotal(),page.getResult());
    }

    /**
     * 批量删除鲜花
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //遍历待删除的鲜花列表，如果存在正在启售的鲜花，就不允许删除
        for(Long id:ids){
            Flower flower=flowerMapper.getById(id);
            if(flower != null && flower.getStatus()== StatusConstant.ENABLE){
                //当前鲜花处于启售状态，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);//抛出不允许删除异常
            }

            if(flower != null){
                flowerMapper.deleteById(id);
                //删除该鲜花的缓存数据
                redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + id);
                //同时删除该鲜花关联的促销缓存
                redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + id);
            }
        }
    }


    /**
     * 根据id查询鲜花
     * @param id
     * @return
     */
    public FlowerVO getByIdWithFlavor(Long id) {
        //根据id查询鲜花数据
        Flower flower = redisClient.queryWithMutex(
                RedisConstants.FLOWER_KEY,                 //缓存key前缀
                id,                                        //鲜花id
                Flower.class,flowerMapper::getById,        //查询方法
                RedisConstants.CACHE_FLOWER_TTL,           //缓存过期时间
                TimeUnit.SECONDS,                          //时间单位
                RedisConstants.MAX_RETRY_COUNT             //最大重试次数
        );
        //将查询到的数据封装到FlowerVO中
        FlowerVO flowerVO=new FlowerVO();
        BeanUtils.copyProperties(flower,flowerVO);
        return flowerVO;
    }

    /**
     * 根据id修改鲜花的信息
     * @param flowerDTO
     */
    @Transactional
    public void update(FlowerDTO flowerDTO) {
        // 先查询该鲜花是否是促销鲜花
        ActivitySale activitySale = activitySaleMapper.getByFlowerId(flowerDTO.getId());
        
        Flower flower=new Flower();
        BeanUtils.copyProperties(flowerDTO,flower);
        flowerMapper.update(flower);

        // 同步更新促销表中对应的鲜花信息（折扣价等）
        if (activitySale != null) {
            ActivitySale activitySaleToUpdate = new ActivitySale();
            activitySaleToUpdate.setId(activitySale.getId());
            if (flowerDTO.getPrice() != null) {
                activitySaleToUpdate.setDiscountPrice(flowerDTO.getPrice());
            }
            activitySaleMapper.update(activitySaleToUpdate);
        }

        //删除该鲜花可能存在的旧缓存数据
        redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + flowerDTO.getId());
        //同时删除该鲜花关联的促销缓存，避免用户端看到旧数据
        redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + flowerDTO.getId());

    }

    /**
     * 批量上架/下架鲜花
     * @param status
     * @param ids
     */
    @Transactional
    public void startOrStop(Integer status, List<Long> ids) {
        for (Long id : ids) {
            Flower flower = Flower.builder().id(id).status(status).build();
            flowerMapper.update(flower);
            //删除该鲜花的缓存数据
            redisClient.deleteByPattern(RedisConstants.FLOWER_KEY + id);
            //同时删除该鲜花关联的促销缓存
            redisClient.deleteByPattern(RedisConstants.ACTIVITY_SALE_BY_FLOWER_KEY + id);
        }
    }

    /**
     * 条件查询鲜花
     * @param flower
     * @return
     */
    public List<FlowerVO> query(Flower flower) {
        List<Flower> flowerList = flowerMapper.list(flower);
        List<FlowerVO> flowerVOList = new ArrayList<>();
        for (Flower f : flowerList) {
            FlowerVO flowerVO = new FlowerVO();
            BeanUtils.copyProperties(f,flowerVO);
            flowerVOList.add(flowerVO);
        }
        return flowerVOList;
    }


    /**
     * 根据分类id查询鲜花（新方法，缓存安全，提供给用户端）
     * @param categoryId
     * @return
     */
    @Override
    public List<FlowerVO> getByCategoryId(Long categoryId) {
        Flower flower = Flower.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE) //查询启售中的鲜花
                .build();

        List<Flower> flowerList = flowerMapper.list(flower);

        //将查询到的鲜花数据封装到FlowerVO中
        List<FlowerVO> flowerVOList = new ArrayList<>();
        for (Flower f : flowerList) {
            FlowerVO flowerVO = new FlowerVO();
            BeanUtils.copyProperties(f,flowerVO);
            flowerVOList.add(flowerVO);
        }

        return flowerVOList;
    }


    /**
     * 根据分类id查询鲜花
     * @param categoryId
     * @return
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
        return flowerMapper.getById(id);
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

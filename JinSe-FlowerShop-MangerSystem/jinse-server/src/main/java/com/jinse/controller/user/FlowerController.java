package com.jinse.controller.user;

import com.jinse.constant.RedisConstants;
import com.jinse.constant.StatusConstant;
import com.jinse.context.BaseContext;
import com.jinse.dto.FlowerPageQueryDTO;
import com.jinse.entity.ActivitySale;
import com.jinse.entity.Flower;
import com.jinse.mapper.ActivitySaleMapper;
import com.jinse.mapper.OrderDetailMapper;
import com.jinse.result.PageResult;
import com.jinse.result.Result;
import com.jinse.service.ActivityService;
import com.jinse.service.FlowerService;
import com.jinse.utils.RedisClient;
import com.jinse.vo.FlowerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController("userFlowerController")
@RequestMapping("/user/flower")
@Slf4j
@Api(tags = "C端-鲜花浏览接口")
public class FlowerController {
    @Autowired
    private FlowerService flowerService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ActivitySaleMapper activitySaleMapper;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 根据分类id查询鲜花
     * @param categoryId
     * @return
     */
    @GetMapping("/getByCategoryId")
    @ApiOperation("根据分类id查询鲜花")
    public Result<List<FlowerVO>> getByCategoryId(Long categoryId) {
        List<FlowerVO> list =flowerService.getByCategoryId(categoryId);
        return Result.success(list);
    }


    /**
     * 分页查询鲜花
     * @param flowerPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询鲜花") //将鲜花名写入dto，然后传到后端，用mapper层的万能条件查询方法进行查询
    public Result<PageResult> pageQuery(FlowerPageQueryDTO flowerPageQueryDTO) {
        PageResult page =flowerService.pageQuery(flowerPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 根据id查询鲜花详情
     * @param id
     * @return
     */
    @GetMapping("/id/{id}")
    @ApiOperation("根据id查询鲜花详情")
    public Result<FlowerVO> getById(@PathVariable Long id) {
        FlowerVO flowerVO = flowerService.getByIdWithFlavor(id);
        ActivitySale sale = activityService.getActivitySaleByFlowerId(id);
        if (sale != null) {
            flowerVO.setPromo(true);
            flowerVO.setOriginalPrice(sale.getOriginalPrice());
            flowerVO.setDiscountPrice(sale.getDiscountPrice());
            flowerVO.setStock(sale.getStock());
            flowerVO.setSale(sale.getSale());
            flowerVO.setLimitPer(sale.getLimitPer());
            flowerVO.setActivityContent(sale.getActivityContent());
            // 查询当前用户已购数量
            Long userId = BaseContext.getCurrentId();
            if (userId != null) {
                Integer purchased = orderDetailMapper.sumPurchasedQuantity(userId, id);
                flowerVO.setPurchasedCount(purchased != null ? purchased : 0);
            }
        } else {
            flowerVO.setPromo(false);
        }
        return Result.success(flowerVO);
    }

    @GetMapping("/shop")
    @ApiOperation("鲜花商城合并查询：促销鲜花+正常鲜花")
    public Result<List<FlowerVO>> shopList() {
        List<FlowerVO> result = new ArrayList<>();
        Set<Long> promoFlowerIds = new HashSet<>();

        List<ActivitySale> ongoingSales = activitySaleMapper.listOngoingActivitySales();
        for (ActivitySale sale : ongoingSales) {
            Flower flower = flowerService.getByIdFlower(sale.getFlowerId());
            if (flower == null || flower.getStatus() != StatusConstant.ENABLE) {
                continue;
            }
            promoFlowerIds.add(flower.getId());
            result.add(FlowerVO.builder()
                    .id(flower.getId())
                    .name(flower.getName())
                    .categoryId(flower.getCategoryId())
                    .price(flower.getPrice())
                    .image(flower.getImage())
                    .description(flower.getDescription())
                    .status(flower.getStatus())
                    .promo(true)
                    .originalPrice(sale.getOriginalPrice())
                    .discountPrice(sale.getDiscountPrice())
                    .stock(sale.getStock())
                    .sale(sale.getSale())
                    .limitPer(sale.getLimitPer())
                    .activityContent(sale.getActivityContent())
                    .build());
        }

        List<FlowerVO> normalFlowers = flowerService.listAllEnabled();
        for (FlowerVO fv : normalFlowers) {
            if (!promoFlowerIds.contains(fv.getId())) {
                fv.setPromo(false);
                result.add(fv);
            }
        }

        return Result.success(result);
    }


}

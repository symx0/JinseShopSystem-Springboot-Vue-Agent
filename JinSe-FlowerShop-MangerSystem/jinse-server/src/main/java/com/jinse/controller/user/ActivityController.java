package com.jinse.controller.user;


import com.jinse.dto.ActivityPageQueryDTO;
import com.jinse.dto.ActivitySalePageQueryDTO;
import com.jinse.entity.Activity;
import com.jinse.entity.ActivitySale;
import com.jinse.entity.Flower;
import com.jinse.mapper.ActivitySaleMapper;
import com.jinse.mapper.FlowerMapper;
import com.jinse.result.PageResult;
import com.jinse.result.Result;
import com.jinse.service.ActivityService;
import com.jinse.vo.ActivitySaleVO;
import com.jinse.vo.ActivityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController("userActivityController")
@RequestMapping("user/activity")
@Slf4j
@RequiredArgsConstructor
@Api(tags= "热门活动")
public class ActivityController {

    @Autowired
    private ActivityService activityService;
    @Autowired
    private FlowerMapper flowerMapper;
    @Autowired
    private ActivitySaleMapper activitySaleMapper;


    /**
     * 分页查询活动（含销量最高商品图片）
     * @param activityPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> PageQuery(ActivityPageQueryDTO activityPageQueryDTO){
        PageResult pageResult=activityService.pageQueryForUser(activityPageQueryDTO);

        // 转换为 ActivityVO，附加热销商品图片
        List<Activity> activities = pageResult.getRecords();
        List<ActivityVO> activityVOList = new ArrayList<>();
        for (Activity activity : activities) {
            ActivityVO vo = new ActivityVO();
            BeanUtils.copyProperties(activity, vo);
            // 查询该活动中销量最高的鲜花图片
            String bestsellerImage = activitySaleMapper.getBestsellerImageByActivityId(activity.getId());
            vo.setBestsellerImage(bestsellerImage);
            activityVOList.add(vo);
        }

        PageResult result = new PageResult(pageResult.getTotal(), activityVOList);
        return Result.success(result);
    }

    /**
     * 根据id查询活动
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询活动")
    public Result<Activity> getById(@PathVariable Long id){
        Activity activity=activityService.getById(id);
        return Result.success(activity);
    }

    /**
     * 查询活动详情（查看活动下的促销商品）
     * @param activitySalePageQueryDTO
     * @return
     */
    @GetMapping("/sale")
    @ApiOperation("查询活动详情(促销商品)")
    public Result<List<ActivitySaleVO>> queryActivitySale(ActivitySalePageQueryDTO activitySalePageQueryDTO){
        PageResult pageResult=activityService.pageQuerySale(activitySalePageQueryDTO);
        List<ActivitySale> sales = (List<ActivitySale>) pageResult.getRecords().stream()
                .map(record -> (ActivitySale) record)
                .collect(Collectors.toList());

        List<ActivitySaleVO> activitySaleVOList=new ArrayList<>();
        for(ActivitySale activitySale:sales){
            Long flowerId=activitySale.getFlowerId();
            // flowerId现在指向促销鲜花记录，直接从flower表获取信息
            Flower flower=flowerMapper.getById(flowerId);

            ActivitySaleVO activitySaleVO=new ActivitySaleVO();
            activitySaleVO.setName(flower.getName());
            activitySaleVO.setPrice(flower.getPrice());
            activitySaleVO.setImage(flower.getImage());
            activitySaleVO.setDescription(flower.getDescription());
            activitySaleVO.setOriginalPrice(activitySale.getOriginalPrice());
            activitySaleVO.setDiscountPrice(activitySale.getDiscountPrice());
            activitySaleVO.setFlowerId(flowerId);
            activitySaleVO.setStock(activitySale.getStock());
            activitySaleVO.setSale(activitySale.getSale());

            activitySaleVOList.add(activitySaleVO);
        }
        return Result.success(activitySaleVOList);
    }
}

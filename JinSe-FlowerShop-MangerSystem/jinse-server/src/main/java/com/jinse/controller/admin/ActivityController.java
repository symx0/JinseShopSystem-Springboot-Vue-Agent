package com.jinse.controller.admin;


import com.jinse.dto.ActivityDTO;
import com.jinse.dto.ActivityPageQueryDTO;
import com.jinse.dto.ActivitySaleDetailDTO;
import com.jinse.dto.ActivitySalePageQueryDTO;
import com.jinse.entity.Activity;
import com.jinse.mapper.ActivitySaleMapper;
import com.jinse.result.PageResult;
import com.jinse.result.Result;
import com.jinse.service.ActivityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("adminActivityController")
@RequestMapping("admin/activity")
@Slf4j
@Api(tags= "热门活动")
public class ActivityController {
    @Autowired
     private ActivityService activityService;
    @Autowired
    private ActivitySaleMapper activitySaleMapper;

    /**
     * 创建活动
     * @param activityDTO
     * @return
     */
        @PostMapping("/create")
        @ApiOperation("创建活动")
        public Result create(@RequestBody ActivityDTO activityDTO){
            activityService.create(activityDTO);
            return Result.success();
        }

    /**
     * 设置活动的促销鲜花表
     * @param activitySaleDetailDTO
     * @return
     */
    @PostMapping("/set")
        @ApiOperation("新增活动的促销鲜花表")
        public Result setActivitySale(@RequestBody ActivitySaleDetailDTO activitySaleDetailDTO){
            activityService.setActivitySale(activitySaleDetailDTO);
            return Result.success();
        }

    @GetMapping("/page")
    @ApiOperation("活动分页查询")
    public Result<PageResult> page(ActivityPageQueryDTO activityPageQueryDTO){
        log.info("活动分页查询：{}", activityPageQueryDTO);
        PageResult pageResult = activityService.pageQuery(activityPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除活动")
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除活动：{}", ids);
        activityService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询活动")
    public Result<Activity> getById(@PathVariable Long id){
        Activity activity = activityService.getById(id);
        return Result.success(activity);
    }

    @PutMapping
    @ApiOperation("更新活动")
    public Result update(@RequestBody ActivityDTO activityDTO){
        log.info("更新活动：{}", activityDTO);
        activityService.update(activityDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改活动状态")
    public Result setStatus(@PathVariable Integer status, Long id){
        log.info("修改活动状态：status={}, id={}", status, id);
        activityService.setStatus(status, id);
        return Result.success();
    }

    @PostMapping("/status/batch/{status}")
    @ApiOperation("批量修改活动状态")
    public Result setStatusBatch(@PathVariable Integer status, @RequestParam List<Long> ids){
        log.info("批量修改活动状态：status={}, ids={}", status, ids);
        activityService.setStatusBatch(status, ids);
        return Result.success();
    }

    @GetMapping("/sale/page")
    @ApiOperation("活动商品分页查询")
    public Result<PageResult> salePage(ActivitySalePageQueryDTO activitySalePageQueryDTO){
        log.info("活动商品分页查询：{}", activitySalePageQueryDTO);
        PageResult pageResult = activityService.pageQuerySale(activitySalePageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping("/sale")
    @ApiOperation("删除活动中的商品")
    public Result deleteSale(Long id, Long flowerId){
        log.info("删除活动商品：id={}, flowerId={}", id, flowerId);
        activityService.deleteActivitySaleById(id, flowerId);

        return Result.success();
    }

    @PutMapping("/sale")
    @ApiOperation("更新活动中的商品")
    public Result updateSale(@RequestBody ActivitySaleDetailDTO activitySaleDetailDTO){
        log.info("更新活动商品：{}", activitySaleDetailDTO);
        activityService.updateActivitySale(activitySaleDetailDTO);
        return Result.success();
    }

    @GetMapping("/stats/{activityId}")
    @ApiOperation("活动销售统计")
    public Result<Map<String, Object>> stats(@PathVariable Long activityId){
        Integer totalSale = activitySaleMapper.sumSaleByActivityId(activityId);
        BigDecimal totalRevenue = activitySaleMapper.sumRevenueByActivityId(activityId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSale", totalSale);
        stats.put("totalRevenue", totalRevenue);
        return Result.success(stats);
    }

}

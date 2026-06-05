package com.jinse.controller.admin;

import com.jinse.dto.CategoryDTO;
import com.jinse.dto.CategoryPageQueryDTO;
import com.jinse.entity.Category;
import com.jinse.result.PageResult;
import com.jinse.result.Result;
import com.jinse.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result<String> save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分页查询：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        log.info("分类分页查询结果：{}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 批量删除分类
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除分类")
    public Result<String> delete(@RequestParam List<Long> ids){
        log.info("批量删除分类：{}", ids);
        categoryService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> update(@RequestBody CategoryDTO categoryDTO){
        categoryService.update(categoryDTO);
        return Result.success();
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 批量启用禁用分类
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/batch/{status}")
    @ApiOperation("批量启用禁用分类")
    public Result<String> batchStartOrStop(@PathVariable("status") Integer status, @RequestParam List<Long> ids){
        log.info("批量启用禁用分类, status={}, ids={}", status, ids);
        categoryService.batchStartOrStop(status, ids);
        return Result.success("操作成功");
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type){
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}

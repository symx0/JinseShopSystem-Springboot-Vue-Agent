package com.jinse.service;

import com.jinse.entity.Category;
import com.jinse.dto.CategoryDTO;
import com.jinse.dto.CategoryPageQueryDTO;
import com.jinse.result.PageResult;

import java.util.List;

public interface CategoryService {

    /**
     * 新增分类
     * @param categoryDTO
     */
    void save(CategoryDTO categoryDTO);

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id删除分类
     * @param id
     */
    void deleteById(Long id);

    /**
     * 批量删除分类
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改分类
     * @param categoryDTO
     */
    void update(CategoryDTO categoryDTO);

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 批量启用禁用分类
     * @param status
     * @param ids
     */
    void batchStartOrStop(Integer status, List<Long> ids);

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}

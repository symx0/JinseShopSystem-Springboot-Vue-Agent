package com.jinse.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jinse.constant.MessageConstant;
import com.jinse.constant.StatusConstant;
import com.jinse.context.BaseContext;
import com.jinse.dto.CategoryDTO;
import com.jinse.dto.CategoryPageQueryDTO;
import com.jinse.entity.Category;
import com.jinse.exception.DeletionNotAllowedException;
import com.jinse.mapper.CategoryMapper;
import com.jinse.mapper.FlowerMapper;
import com.jinse.result.PageResult;
import com.jinse.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private FlowerMapper flowerMapper;


    /**
     * 新增分类
     * @param categoryDTO
     */
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        //属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        //分类状态默认为禁用状态0
        category.setStatus(StatusConstant.DISABLE);

        /**
        //设置创建时间、修改时间、创建人、修改人
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        */
        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        //下一条sql进行分页，自动加入limit关键字分页
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id删除分类
     * @param id
     */
    public void deleteById(Long id) {
        deleteBatch(Collections.singletonList(id));
    }

    /**
     * 批量删除分类
     * @param ids
     */
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            Integer count = flowerMapper.countByCategoryId(id);
            if (count > 0) {
                throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
            }
        }
        categoryMapper.deleteByIds(ids);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);

        /**
        //设置修改时间、修改人
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
       */
        categoryMapper.update(category);
    }

    /**
     * 批量启用禁用分类
     * @param status
     * @param ids
     */
    @Transactional
    public void batchStartOrStop(Integer status, List<Long> ids) {
        for (Long id : ids) {
            Category category = Category.builder().id(id).status(status).build();
            categoryMapper.update(category);
        }
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}

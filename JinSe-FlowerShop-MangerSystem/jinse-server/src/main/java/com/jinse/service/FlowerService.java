package com.jinse.service;

import com.jinse.entity.Flower;
import com.jinse.result.PageResult;
import com.jinse.vo.FlowerVO;
import com.jinse.dto.FlowerDTO;
import com.jinse.dto.FlowerPageQueryDTO;

import java.util.List;

public interface FlowerService {

    //TODO：修改方法名
    /**
     * 新增鲜花
     * @param flowerDTO
     */
    public void save(FlowerDTO flowerDTO);

    /**
     * 鲜花分页查询
     * @param flowerPageQueryDTO
     * @return
     */
    PageResult pageQuery(FlowerPageQueryDTO flowerPageQueryDTO);


    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 批量上架/下架鲜花
     * @param status
     * @param ids
     */
    void startOrStop(Integer status, List<Long> ids);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    FlowerVO getByIdWithFlavor(Long id);


    /**
     * 修改鲜花
     * @param flowerhDTO
     */
    void update(FlowerDTO flowerhDTO);


    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Flower> list(Long categoryId);



    /**
     * 条件查询菜品和口味
     * @param flower
     * @return
     */
    List<FlowerVO> query(Flower flower);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<FlowerVO> getByCategoryId(Long categoryId);

    Flower getByIdFlower(Long id);

    List<FlowerVO> listAllEnabled();

}

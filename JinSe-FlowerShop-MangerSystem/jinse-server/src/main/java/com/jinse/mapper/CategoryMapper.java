package com.jinse.mapper;

import com.github.pagehelper.Page;
import com.jinse.annotation.AutoFill;
import com.jinse.dto.CategoryPageQueryDTO;
import com.jinse.entity.Category;
import com.jinse.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 插入数据
     * @param category
     */
    @Insert("insert into category(name, sort, status, image, create_time, update_time, create_user, update_user)" +
            " VALUES" +
            " ( #{name}, #{sort}, #{status}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(value=OperationType.INSERT)
    void insert(Category category);

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据id删除分类
     * @param id
     */
    @Delete("delete from category where id = #{id}")
    void deleteById(Long id);

    void deleteByIds(List<Long> ids);

    /**
     * 根据id修改分类
     * @param category
     */
    @AutoFill(value=OperationType.UPDATE)
    void update(Category category);

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    List<Category> list(Integer type);
}

package com.jinse.mapper;


import com.jinse.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);


    /**
     * 更新商品数量
     * @param cart
     */
    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumberById(ShoppingCart cart);


    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, flower_id, amount, number, create_time)" +
    "values (#{name},#{image},#{userId},#{flowerId},#{amount},#{number},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where id=#{id}")
    void deleteById(Long id);


    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);
}

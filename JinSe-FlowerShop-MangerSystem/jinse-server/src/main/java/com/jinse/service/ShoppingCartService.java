package com.jinse.service;

import com.jinse.entity.ShoppingCart;
import com.jinse.dto.ShoppingCartDTO;
import com.jinse.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     */
    void cleanShoppingCart();

    /**
     * 减少购物车商品数量
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}

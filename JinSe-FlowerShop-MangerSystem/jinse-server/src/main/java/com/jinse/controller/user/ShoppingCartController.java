package com.jinse.controller.user;


import com.jinse.dto.ShoppingCartDTO;
import com.jinse.entity.ShoppingCart;
import com.jinse.result.Result;
import com.jinse.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
@Api(tags="C端购物车接口")
public class ShoppingCartController {

    @Autowired
    ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车，商品信息为:{}",shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
        List<ShoppingCart> list=shoppingCartService.showShoppingCart();
        return Result.success(list);
    }


    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result clean(){
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    /**
     * 减少购物车商品数量
     */
    @PostMapping("/sub")
    @ApiOperation("减少购物车商品数量")
    public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("减少购物车商品数量：{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return Result.success();
    }
}

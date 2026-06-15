package com.jinse.service.impl;


import com.jinse.constant.MessageConstant;
import com.jinse.context.BaseContext;
import com.jinse.dto.ShoppingCartDTO;
import com.jinse.entity.Activity;
import com.jinse.entity.ActivitySale;
import com.jinse.entity.Flower;
import com.jinse.entity.ShoppingCart;
import com.jinse.exception.BaseException;
import com.jinse.mapper.ActivitySaleMapper;
import com.jinse.mapper.ActivityMapper;
import com.jinse.mapper.FlowerMapper;
import com.jinse.mapper.OrderDetailMapper;
import com.jinse.mapper.ShoppingCartMapper;
import com.jinse.service.ActivityService;
import com.jinse.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private FlowerMapper flowerMapper;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前商品是否已经存在于购物车里
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list =shoppingCartMapper.list(shoppingCart);

        // 计算加入购物车后的总数量
        int currentCartNumber = 0;
        if(list!=null&&list.size()>0){
            currentCartNumber = list.get(0).getNumber();
        }
        int addNumber = shoppingCartDTO.getNumber() != null ? shoppingCartDTO.getNumber() : 1;
        int totalNumber = currentCartNumber + addNumber;

        // 检查是否为促销商品，如果是则进行限购和库存校验
        Long flowerId = shoppingCartDTO.getFlowerId();
        if(flowerId != null){
            ActivitySale activitySale = activityService.getActivitySaleByFlowerId(flowerId);
            if(activitySale != null){
                Activity activity = activityService.getById(activitySale.getActivityId());
                if(activity != null && activity.getLimitPer() != null){
                    // 查询用户已购买该促销商品的累计数量（已下单的）
                    Integer purchased = orderDetailMapper.sumPurchasedQuantity(userId, flowerId);
                    if(purchased == null)
                        purchased = 0;
                    // 加上购物车中的数量
                    if(purchased + totalNumber > activity.getLimitPer()){
                        throw new BaseException(MessageConstant.PROMO_EXCEED_LIMIT + "，限购" + activity.getLimitPer() + "件，您已购买" + purchased + "件，购物车中" + currentCartNumber + "件");
                    }
                }
                // 库存校验
                if(activitySale.getStock() != null && totalNumber > activitySale.getStock()){
                    throw new BaseException(MessageConstant.PROMO_STOCK_INSUFFICIENT + "，仅剩" + activitySale.getStock() + "件");
                }
            }
        }

        //如果已经存在，则增加商品数量，不进行新增操作（update）
        if(list!=null&&list.size()>0){
            ShoppingCart cart =list.get(0);
            cart.setNumber(cart.getNumber() + addNumber);
            shoppingCartMapper.updateNumberById(cart);
            return;
        }
        //如果不存在，则添加到购物车，数量默认为1（insert）
        if(flowerId!=null){
            Flower flower=flowerMapper.getById(flowerId); //根据菜品id查询菜品对象，拿到该菜品的全部数据
            if(flower == null) {
                throw new BaseException("鲜花商品不存在，ID: " + flowerId);
            }
            shoppingCart.setName(flower.getName());
            shoppingCart.setImage(flower.getImage());
            shoppingCart.setAmount(flower.getPrice());
        }else{
            throw new BaseException(MessageConstant.FLOWER_NOT_FOUND);
        }
        shoppingCart.setNumber(addNumber);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 查看购物车
     * @return
     */
    public List<ShoppingCart> showShoppingCart() {
        //获取当前用户的id
        Long userId =BaseContext.getCurrentId();
        ShoppingCart shoppingCart=ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart>list = shoppingCartMapper.list(shoppingCart); //根据用户id查询该用户的购物车
        return list;
    }

    /**
     * 清空购物车
     */
    public void cleanShoppingCart() {
        Long userId=BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    /**
     * 减少购物车商品数量
     */
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            int number = (cart.getNumber() == null ? 0 : cart.getNumber()) - 1;
            if (number <= 0) {
                shoppingCartMapper.deleteById(cart.getId());
            } else {
                cart.setNumber(number);
                shoppingCartMapper.updateNumberById(cart);
            }
        }
    }
}

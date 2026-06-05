package com.jinse.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ActivitySaleDetailDTO implements Serializable {


    private Long id;            //活动销售记录id

    private Long activityId;     //活动id

    private Long flowerId;       //原始鲜花id

    private BigDecimal originalPrice; //原价

    private BigDecimal discountPrice; //折扣价

    private Integer stock;  //库存

    private Integer sale; //已售数量

    // 原始鲜花信息（前端直接传递，省去后端查询）
    private String flowerName;
    private Long categoryId;
    private String flowerImage;
    private String flowerDescription;

}

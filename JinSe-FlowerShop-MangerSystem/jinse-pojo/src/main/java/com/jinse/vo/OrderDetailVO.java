package com.jinse.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO  implements Serializable {

    private Long id;

    //鲜花名称
    private String flowerName;

    //数量
    private Integer number;

    //金额
    private BigDecimal amount;

    //图片
    private String image;


}

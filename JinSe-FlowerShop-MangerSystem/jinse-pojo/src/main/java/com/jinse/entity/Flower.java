package com.jinse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flower implements Serializable {

    private static final long serialVersionUID = 1L;

    //鲜花ID
    private Long id;
    //鲜花名称
    private String name;
    //鲜花分类id
    private Long categoryId;
    //鲜花价格
    private BigDecimal price;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 停售 1 起售
    private Integer status;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
    // 创建人
    private Long createUser;
    // 更新人
    private Long updateUser;

}

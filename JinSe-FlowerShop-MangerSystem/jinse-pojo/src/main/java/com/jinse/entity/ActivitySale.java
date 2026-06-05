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
public class ActivitySale implements Serializable {

    private static final long serialVersionUID = 1L;
    // 活动销售ID
    private Long id;
    // 活动ID
    private Long activityId;
    // 花束ID
    private Long flowerId;
    // 原价
    private BigDecimal originalPrice;
    // 优惠价
    private BigDecimal discountPrice;
    // 库存数量
    private Integer stock;
    // 已售数量
    private Integer sale;
    // 创建时间
    private LocalDateTime createTime;
    // 更新时间
    private LocalDateTime updateTime;
    // 创建人
    private Long createUser;
    // 更新人
    private Long updateUser;
    // 版本号（用于乐观锁）
    private Integer version;

    private String activityContent;
    private Integer limitPer;


}

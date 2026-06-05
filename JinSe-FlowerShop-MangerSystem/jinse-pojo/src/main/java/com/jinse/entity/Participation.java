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
public class Participation implements Serializable {

    private static final long serialVersionUID = 1L;
    // 参与ID
    private Long id;
    // 活动ID
    private Long activityId;
    // 用户ID
    private Long userId;
    // 数量
    private Integer quantity;
    // 订单ID
    private Long orderId;
    // 创建时间
    private LocalDateTime createTime;

}

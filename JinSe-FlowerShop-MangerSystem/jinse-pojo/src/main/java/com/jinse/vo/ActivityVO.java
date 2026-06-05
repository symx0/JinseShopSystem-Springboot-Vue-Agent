package com.jinse.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // 活动ID
    private Long id;
    // 活动开始时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    // 活动结束时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    // 活动状态 0 未开始 1 进行中 2 已结束
    private Integer status;
    // 活动简介
    private String content;
    // 限购数量
    private Integer limitPer;
    // 活动中销量最高商品的图片
    private String bestsellerImage;
}
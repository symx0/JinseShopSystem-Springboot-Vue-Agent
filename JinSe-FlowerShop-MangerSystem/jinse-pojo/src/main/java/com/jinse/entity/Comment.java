package com.jinse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 评论
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    //评论id
    private Long id;
    // 用户ID
    private Long userId;
    // 花束ID
    private Long flowerId;
    //评价等级
    private Integer rating;
    //评论内容
    private String content;
    //创建时间
    private LocalDateTime createTime;
    //点赞数
    private int likeCount;
    //回复数
    private int replyCount;

}

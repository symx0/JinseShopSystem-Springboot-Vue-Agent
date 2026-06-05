package com.jinse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户点赞评论记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserComment implements Serializable {

    private static final long serialVersionUID = 1L;

    // 用户点赞记录ID
    private Long id;
    // 评论ID
    private Long commentId;
    // 用户ID
    private Long userId;

}

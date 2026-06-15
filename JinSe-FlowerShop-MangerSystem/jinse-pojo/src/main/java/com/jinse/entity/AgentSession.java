package com.jinse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI导购会话实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 会话标题（首条消息摘要） */
    private String title;

    /** 是否活跃：1=活跃 0=已删除 */
    private Integer isActive;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后活跃时间 */
    private LocalDateTime updatedAt;
}

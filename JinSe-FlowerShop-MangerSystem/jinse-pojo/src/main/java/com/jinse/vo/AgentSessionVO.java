package com.jinse.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI导购会话视图对象（返回前端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentSessionVO {

    /** 数据库自增主键，也是 Agent 端会话文件的命名依据 */
    private Long id;

    /** 会话标题 */
    private String title;

    /** 是否活跃 */
    private Integer isActive;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后活跃时间 */
    private LocalDateTime updatedAt;
}

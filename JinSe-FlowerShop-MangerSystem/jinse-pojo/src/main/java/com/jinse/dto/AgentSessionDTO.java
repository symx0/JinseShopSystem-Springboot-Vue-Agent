package com.jinse.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * AI导购会话请求DTO
 */
@Data
public class AgentSessionDTO {

    /** 会话唯一标识（创建时可选，由后端生成） */
    private String sessionId;

    /** 会话标题 */
    @NotBlank(message = "会话标题不能为空")
    private String title;
}

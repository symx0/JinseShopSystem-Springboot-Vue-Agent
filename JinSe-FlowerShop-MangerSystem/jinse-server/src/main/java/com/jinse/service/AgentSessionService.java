package com.jinse.service;

import com.jinse.vo.AgentSessionVO;

import java.util.List;

/**
 * AI导购会话服务接口
 */
public interface AgentSessionService {

    /**
     * 获取用户所有会话列表（按最后活跃时间倒序）
     */
    List<AgentSessionVO> listSessions(Long userId);

    /**
     * 创建新会话，返回包含DB自增ID的VO（Agent端以此ID命名会话文件）
     */
    AgentSessionVO createSession(Long userId, String title);

    /**
     * 删除会话（软删除）
     */
    void deleteSession(Long userId, Long sessionId);

    /**
     * 更新会话活跃时间
     */
    void touchSession(Long sessionId);

    /**
     * 更新会话标题
     */
    void renameSession(Long sessionId, String title);
}
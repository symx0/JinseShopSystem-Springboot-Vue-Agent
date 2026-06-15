package com.jinse.mapper;

import com.jinse.entity.AgentSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI导购会话 Mapper
 */
@Mapper
public interface AgentSessionMapper {

    /**
     * 根据用户ID查询所有活跃会话（按最后活跃时间倒序）
     */
    List<AgentSession> listByUserId(@Param("userId") Long userId);

    /**
     * 根据主键查询会话
     */
    AgentSession selectById(@Param("id") Long id);

    /**
     * 新增会话（自动回填自增ID到 session.id）
     */
    void insert(AgentSession session);

    /**
     * 更新最后活跃时间
     */
    void touch(@Param("id") Long id);

    /**
     * 更新会话标题
     */
    void updateTitle(@Param("id") Long id, @Param("title") String title);

    /**
     * 软删除（设 is_active = 0）
     */
    void softDelete(@Param("userId") Long userId, @Param("id") Long id);
}
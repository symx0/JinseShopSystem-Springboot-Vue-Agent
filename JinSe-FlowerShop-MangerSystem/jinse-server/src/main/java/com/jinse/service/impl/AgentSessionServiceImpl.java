package com.jinse.service.impl;

import com.jinse.entity.AgentSession;
import com.jinse.mapper.AgentSessionMapper;
import com.jinse.service.AgentSessionService;
import com.jinse.vo.AgentSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI导购会话服务实现
 * <p>
 * 会话标识统一使用 DB 自增主键 id，Agent 端以此 id 命名会话文件。
 * </p>
 */
@Service
@Slf4j
public class AgentSessionServiceImpl implements AgentSessionService {

    @Autowired
    private AgentSessionMapper agentSessionMapper;

    @Override
    public List<AgentSessionVO> listSessions(Long userId) {
        List<AgentSession> sessions = agentSessionMapper.listByUserId(userId);
        return sessions.stream().map(s -> AgentSessionVO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .isActive(s.getIsActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgentSessionVO createSession(Long userId, String title) {
        AgentSession session = AgentSession.builder()
                .userId(userId)
                .title(title != null ? title : "新会话")
                .build();

        // INSERT → MyBatis 自动回填自增 id 到 session.id
        agentSessionMapper.insert(session);

        // 重新查询以获取数据库生成的 created_at / updated_at
        AgentSession saved = agentSessionMapper.selectById(session.getId());

        log.info("创建AI导购会话: id={}, userId={}, title={}", session.getId(), userId, session.getTitle());

        return AgentSessionVO.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .isActive(saved.getIsActive())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteSession(Long userId, Long sessionId) {
        agentSessionMapper.softDelete(userId, sessionId);
        log.info("删除AI导购会话: userId={}, sessionId={}", userId, sessionId);
    }

    @Override
    public void touchSession(Long sessionId) {
        agentSessionMapper.touch(sessionId);
    }

    @Override
    public void renameSession(Long sessionId, String title) {
        agentSessionMapper.updateTitle(sessionId, title);
        log.info("重命名会话: id={}, title={}", sessionId, title);
    }
}
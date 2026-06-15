package com.jinse.controller.user;

import com.jinse.context.BaseContext;
import com.jinse.result.Result;
import com.jinse.service.AgentSessionService;
import com.jinse.vo.AgentSessionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI导购会话管理接口
 * <p>
 * 会话标识统一使用 DB 自增主键（Long），Agent 端以此命名会话文件。
 * </p>
 */
@RestController("userAgentSessionController")
@Slf4j
@RequestMapping("/user/agent")
@Api(tags = "C端AI导购会话接口")
public class AgentSessionController {

    @Autowired
    private AgentSessionService agentSessionService;

    /**
     * 获取当前用户的所有会话列表
     */
    @GetMapping("/sessions")
    @ApiOperation("获取AI导购会话列表")
    public Result<List<AgentSessionVO>> listSessions() {
        Long userId = BaseContext.getCurrentId();
        log.info("查询用户AI会话列表: userId={}", userId);
        List<AgentSessionVO> sessions = agentSessionService.listSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 创建新会话，返回包含 DB 自增 ID 的 VO
     */
    @PostMapping("/session")
    @ApiOperation("创建AI导购会话")
    public Result<AgentSessionVO> createSession(@RequestBody Map<String, String> body) {
        Long userId = BaseContext.getCurrentId();
        String title = body != null ? body.getOrDefault("title", "新会话") : "新会话";
        log.info("创建AI导购会话: userId={}, title={}", userId, title);
        AgentSessionVO session = agentSessionService.createSession(userId, title);
        return Result.success(session);
    }

    /**
     * 更新会话活跃时间（每次对话时调用）
     */
    @PostMapping("/session/{sessionId}/touch")
    @ApiOperation("更新会话活跃时间")
    public Result touchSession(@PathVariable Long sessionId) {
        agentSessionService.touchSession(sessionId);
        return Result.success();
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/session/{sessionId}/title")
    @ApiOperation("更新会话标题")
    public Result renameSession(@PathVariable Long sessionId, @RequestBody Map<String, String> body) {
        String title = body != null ? body.get("title") : null;
        if (title == null || title.trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        agentSessionService.renameSession(sessionId, title);
        return Result.success();
    }

    /**
     * 删除会话（软删除）
     */
    @DeleteMapping("/session/{sessionId}")
    @ApiOperation("删除AI导购会话")
    public Result deleteSession(@PathVariable Long sessionId) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除AI导购会话: userId={}, sessionId={}", userId, sessionId);
        agentSessionService.deleteSession(userId, sessionId);
        return Result.success();
    }
}
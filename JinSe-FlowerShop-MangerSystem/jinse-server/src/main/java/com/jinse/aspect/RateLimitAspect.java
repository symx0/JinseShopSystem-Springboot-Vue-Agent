package com.jinse.aspect;

import com.jinse.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 滑动窗口限流切面
 * 使用 Redis ZSET 实现精确的滑动窗口计数
 * 
 * 原理：将每次请求的时间戳作为 score 存入 ZSET，
 * 每次请求时删除窗口外的旧记录，统计窗口内剩余记录数
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        int window = rateLimit.window();
        int maxCount = rateLimit.maxCount();

        long now = System.currentTimeMillis();
        long windowStart = now - window * 1000L;

        String redisKey = RATE_LIMIT_PREFIX + key;

        // Lua 脚本保证原子性：删除窗口外记录 + 统计当前窗口内记录数 + 添加当前请求
        String luaScript =
                "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, tonumber(ARGV[1])) " +
                "local count = redis.call('ZCARD', KEYS[1]) " +
                "if count < tonumber(ARGV[2]) then " +
                "    redis.call('ZADD', KEYS[1], tonumber(ARGV[3]), ARGV[4]) " +
                "    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[5])) " +
                "    return 1 " +
                "else " +
                "    return 0 " +
                "end";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = (Long) stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(redisKey),
                String.valueOf(windowStart),
                String.valueOf(maxCount),
                String.valueOf(now),
                String.valueOf(now),
                String.valueOf(window + 1)
        );

        if (result != null && result == 1) {
            return joinPoint.proceed();
        } else {
            log.warn("限流触发，key={}, window={}s, maxCount={}", key, window, maxCount);
            throw new RuntimeException(rateLimit.message());
        }
    }

    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        String baseKey = className + ":" + methodName;

        // 如果注解中指定了动态 key，使用 SpEL 解析
        String keyExpression = rateLimit.key();
        if (keyExpression != null && !keyExpression.isEmpty()) {
            String resolvedKey = parseSpel(keyExpression, joinPoint, method);
            baseKey = baseKey + ":" + resolvedKey;
        }

        return baseKey;
    }

    private String parseSpel(String expression, ProceedingJoinPoint joinPoint, Method method) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(expression);

            StandardEvaluationContext context = new StandardEvaluationContext();
            // 设置方法参数名发现器
            DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
            String[] paramNames = discoverer.getParameterNames(method);
            Object[] args = joinPoint.getArgs();

            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            Object value = exp.getValue(context);
            return value != null ? value.toString() : "unknown";
        } catch (Exception e) {
            log.warn("SpEL 表达式解析失败: {}", expression);
            return "unknown";
        }
    }
}
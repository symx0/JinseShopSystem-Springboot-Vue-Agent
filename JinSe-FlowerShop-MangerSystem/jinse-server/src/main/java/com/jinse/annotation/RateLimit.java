package com.jinse.annotation;

import java.lang.annotation.*;

/**
 * 滑动窗口限流注解
 * 基于 Redis ZSET 实现，支持方法级别的限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 限流标识 key（支持 SpEL 表达式，如 #userId） */
    String key() default "";

    /** 时间窗口，单位秒 */
    int window() default 1;

    /** 窗口内最大请求数 */
    int maxCount() default 10;

    /** 限流提示信息 */
    String message() default "请求过于频繁，请稍后再试";
}
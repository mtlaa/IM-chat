package com.mtlaa.mtchat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 频控限流注解，有3种实现：固定时间间隔（默认）、滑动窗口、令牌桶
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    /**
     * 频控key的前缀。默认使用方法的全限定名；指定值则锁的key不会加上方法全限定名，可用于全局、多方法的频控
     */
    String prefix() default "";

    /**
     * 频控时间。当使用令牌桶时，不起作用
     */
    int time() default 30;
    /**
     * 频控次数。当使用令牌桶时，为令牌桶的容量
     */
    int count() default 3;
    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 当策略为令牌桶时，令牌每秒的生成个数
     */
    double rate() default 1;

    /**
     * 频控的范围
     *         <p>ALL 根据prefix+key进行频控（默认）
     *         <p>UID 根据调用者的UID，即prefix+key+UID进行频控
     *         <P>IP 根据调用IP进行频控
     *         <p>SpEL 根据该方法的调用参数进行频控 （需要同时指定key，不指定则与ALL相同）
     * @see Target
     */
    Target target() default Target.ALL;

    /**
     * SpEL表达式
     */
    String key() default "";

    /**
     * 频控策略
     * @see Policy
     */
    Policy policy() default Policy.FIXED_TIME;


    enum Target{
        ALL,
        UID,
        IP,
        SpEL
    }

    enum Policy{
        FIXED_TIME,
        SLIDING_WINDOW,
        TOKEN_BUCKET
    }
}

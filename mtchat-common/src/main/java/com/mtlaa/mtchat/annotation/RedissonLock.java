package com.mtlaa.mtchat.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedissonLock {
    /**
     * 锁的前缀
     */
    String prefix() default "redisson_lock";

    /**
     * 锁的key。（字符串，可以是SpEL表达式）
     */
    String key() default "";

    /**
     * 获取锁的等待时间，如果在指定时间内没有获取到锁就放弃执行，默认 -1 代表不等待。（不是锁的持有时间，redisson不设置锁持有时间时有看门狗自动续期）
     */
    int waitTime() default -1;

    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
}

package com.mtlaa.mychat.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Create 2023/12/13 14:18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedissonLock {
    /**
     * 分布式锁key的前缀
     */
    String prefixKey() default "";
    /**
     * springEL表达式，用来生成key
     */
    String key();
    /**
     * 等待锁的时间，默认为-1，表示不等待。redisson默认也是-1
     */
    int waitTime() default -1;
    /**
     * 等待锁的时间单位，默认毫秒
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
}

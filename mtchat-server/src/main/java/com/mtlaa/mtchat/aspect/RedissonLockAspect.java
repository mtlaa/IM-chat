package com.mtlaa.mtchat.aspect;

import com.mtlaa.mtchat.annotation.RedissonLock;
import com.mtlaa.mtchat.utils.LockService;
import com.mtlaa.mtchat.utils.SpELUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 分布式锁的切面，生成锁的key，然后加锁执行原方法
 * @see RedissonLock
 */
@Aspect
@Order(0)
@Component
public class RedissonLockAspect {
    @Autowired
    private LockService lockService;

    @Around("@annotation(com.mtlaa.mtchat.annotation.RedissonLock)")
    public Object aroundLock(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
        String prefix = StringUtils.isBlank(redissonLock.prefix()) ?
                SpELUtil.getMethodName(method) : redissonLock.prefix();
        String key = prefix + ":" + SpELUtil.parseSpEL(method, redissonLock.key(), joinPoint.getArgs());

        return lockService.executeWithLockThrows(key, redissonLock.waitTime(), redissonLock.unit(), joinPoint::proceed);
    }
}

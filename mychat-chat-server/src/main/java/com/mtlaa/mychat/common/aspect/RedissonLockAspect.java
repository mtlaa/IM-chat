package com.mtlaa.mychat.common.aspect;

import cn.hutool.core.util.StrUtil;
import com.mtlaa.mychat.common.annotation.RedissonLock;
import com.mtlaa.mychat.common.service.LockService;
import com.mtlaa.mychat.common.utils.SpElUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Create 2023/12/13 14:24
 */
@Component
@Aspect
@Order(0)
public class RedissonLockAspect {
    @Autowired
    private LockService lockService;
    /**
     * 切入点为使用了该注解的方法
     */
    @Pointcut("@annotation(com.mtlaa.mychat.common.annotation.RedissonLock)")
    private void p(){}
    @Around("p()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 生成key
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
        String prefix = StrUtil.isBlank(redissonLock.prefixKey()) ?
                SpElUtils.getMethodKey(method) : redissonLock.prefixKey();
        String key = SpElUtils.parseSpEl(method, joinPoint.getArgs(), redissonLock.key());
        // 加锁调用原方法
        return lockService.executeWithLockThrows(prefix + ":" + key,
                redissonLock.waitTime(), redissonLock.unit(), joinPoint::proceed);
    }
}

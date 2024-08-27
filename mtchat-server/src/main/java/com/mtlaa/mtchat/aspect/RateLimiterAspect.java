package com.mtlaa.mtchat.aspect;

import com.mtlaa.mtchat.annotation.RateLimiter;
import com.mtlaa.mtchat.strategy.ratelimiter.AbstractRateLimiter;
import com.mtlaa.mtchat.strategy.ratelimiter.RateLimiterPolicyFactory;
import com.mtlaa.mtchat.utils.RequestHolder;
import com.mtlaa.mtchat.utils.SpELUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Order(0)
@Component
@Slf4j
public class RateLimiterAspect {

    @Around("@annotation(com.mtlaa.mtchat.annotation.RateLimiter)")
    public Object rateLimiterAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
        String key = getKey(joinPoint, method, rateLimiter);
//        log.info("RateLimiter key: {}", key);

        AbstractRateLimiter rateLimiterPolicy = RateLimiterPolicyFactory.get(rateLimiter.policy());
        rateLimiterPolicy.checkRateLimiter(rateLimiter, key);
        return joinPoint.proceed();
    }

    /**
     * 根据注解中指定的限流 Target，生成 Key
     * @return 限流的Key
     */
    private String getKey(ProceedingJoinPoint joinPoint, Method method, RateLimiter rateLimiter) {
        // key的前缀
        String key = "rateLimiter:" +
                (StringUtils.isBlank(rateLimiter.prefix()) ? SpELUtil.getMethodName(method) : rateLimiter.prefix());
        switch (rateLimiter.target()){
            case ALL:
                key = key + "#ALL:" + rateLimiter.key();
                break;
            case UID:
                key = key + "#UID:" + RequestHolder.get().getUid() + ":" + rateLimiter.key();
                break;
            case IP:
                key = key + "#IP:" + RequestHolder.get().getIp() + ":" + rateLimiter.key();
                break;
            case SpEL:
                key = key + "#SpEL:" + SpELUtil.parseSpEL(method, rateLimiter.key(), joinPoint.getArgs());
        }
        return key;
    }
}

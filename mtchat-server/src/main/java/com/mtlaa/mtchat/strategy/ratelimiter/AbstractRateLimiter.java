package com.mtlaa.mtchat.strategy.ratelimiter;

import com.mtlaa.mtchat.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.Objects;


/**
 * 限流策略的抽象模板类：策略模式
 */
public abstract class AbstractRateLimiter {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 初始化限流策略静态工厂
     */
    @PostConstruct
    public void init(){
        RateLimiterPolicyFactory.register(getType(), this);
    }

    /**
     * 获取策略的类型
     * @return 策略类型
     */
    public abstract RateLimiter.Policy getType();

    /**
     * 执行限流检查，被限流会直接抛出异常
     * @param rateLimiter 限流注解
     * @param key 限流的key
     */
    public abstract void checkRateLimiter(RateLimiter rateLimiter, String key);

    /**
     * 执行暂存在Redis的Lua脚本，返回一个 Long 结果，根据结果判断是否超过限制
     * @param scriptSha 脚本的哈希值
     * @param numKeys key的数量
     * @param keysAndArgs key和参数的字节数组。（通过字符串转字节）
     * @return 计数 Long
     */
    protected Long executeScript(String scriptSha, int numKeys, byte[]... keysAndArgs){
        return Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection()
                .evalSha(scriptSha, ReturnType.INTEGER, numKeys, keysAndArgs);
    }
}

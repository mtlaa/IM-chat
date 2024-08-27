package com.mtlaa.mtchat.strategy.ratelimiter;

import com.mtlaa.mtchat.annotation.RateLimiter;
import com.mtlaa.mtchat.config.RedisConfig;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.exception.CommonErrorEnum;
import org.springframework.stereotype.Component;

@Component
public class FixedTimePolicy extends AbstractRateLimiter{
    @Override
    public RateLimiter.Policy getType() {
        return RateLimiter.Policy.FIXED_TIME;
    }

    /**
     * 如果当前key的计数已经超过count，则抛出异常，代表被限制
     * @param rateLimiter 限流注解
     * @param key 限流的key
     */
    @Override
    public void checkRateLimiter(RateLimiter rateLimiter, String key) {
        int count = rateLimiter.count();
        long time = rateLimiter.unit().toMillis(rateLimiter.time());
        Long ret = executeScript(RedisConfig.FixedTimeScriptSha,
                1, key.getBytes(), Long.toString(time).getBytes(), Integer.toString(count).getBytes());
        if (ret != null && ret > count){
            throw new BusinessException(CommonErrorEnum.FREQUENCY_LIMIT);
        }
    }
}

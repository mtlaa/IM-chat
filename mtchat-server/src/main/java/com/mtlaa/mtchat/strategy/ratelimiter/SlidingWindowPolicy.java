package com.mtlaa.mtchat.strategy.ratelimiter;

import com.mtlaa.mtchat.annotation.RateLimiter;
import com.mtlaa.mtchat.config.RedisConfig;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.exception.CommonErrorEnum;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SlidingWindowPolicy extends AbstractRateLimiter{
    @Override
    public RateLimiter.Policy getType() {
        return RateLimiter.Policy.SLIDING_WINDOW;
    }

    /**
     * 统计当前时间窗口内有多少次访问记录，超过count则限流
     * @param rateLimiter 限流注解
     * @param key 限流的key
     */
    @Override
    public void checkRateLimiter(RateLimiter rateLimiter, String key) {
        int count = rateLimiter.count();
        Long time = rateLimiter.unit().toMillis(rateLimiter.time());
        Long now = System.currentTimeMillis();
        long start = now - time;

        Long ret = executeScript(RedisConfig.SlidingWindowScriptSha, 1,
                key.getBytes(), time.toString().getBytes(), Long.toString(start).getBytes(), now.toString().getBytes(),
                UUID.randomUUID().toString().getBytes(), Integer.toString(count).getBytes());
        if (ret != null && ret > count){
            throw new BusinessException(CommonErrorEnum.FREQUENCY_LIMIT);
        }
    }
}

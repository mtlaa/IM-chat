package com.mtlaa.mtchat.strategy.ratelimiter;

import com.mtlaa.mtchat.annotation.RateLimiter;
import com.mtlaa.mtchat.config.RedisConfig;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.exception.CommonErrorEnum;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenBucketPolicy extends AbstractRateLimiter{
    @Override
    public RateLimiter.Policy getType() {
        return RateLimiter.Policy.TOKEN_BUCKET;
    }

    /**
     * 执行脚本返回 1 代表通过；返回 0 限流
     * @param rateLimiter 限流注解
     * @param key 限流的key
     */
    @Override
    public void checkRateLimiter(RateLimiter rateLimiter, String key) {
        int count = rateLimiter.count();
        double rate = rateLimiter.rate();
        // key的过期时间要比填满一个令牌桶的时间要长，否则存在临界状态流量过大的问题
        long timeout = TimeUnit.SECONDS.toMillis((long) (count / rate + 5));

        Long ret = executeScript(RedisConfig.TokenBucketScriptSha, 1,
                key.getBytes(), Integer.toString(count).getBytes(),
                Double.toString(rate).getBytes(), Long.toString(timeout).getBytes());
        if (ret != null && ret.equals(0L)){
            throw new BusinessException(CommonErrorEnum.FREQUENCY_LIMIT);
        }
    }
}

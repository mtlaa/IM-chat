package com.mtlaa.mtchat.strategy.ratelimiter;

import com.mtlaa.mtchat.annotation.RateLimiter;

import java.util.HashMap;
import java.util.Map;


public class RateLimiterPolicyFactory {
    private static final Map<RateLimiter.Policy, AbstractRateLimiter> map = new HashMap<>();
    public static void register(RateLimiter.Policy type, AbstractRateLimiter rateLimiter){
        map.put(type, rateLimiter);
    }
    public static AbstractRateLimiter get(RateLimiter.Policy type){
        return map.get(type);
    }
}

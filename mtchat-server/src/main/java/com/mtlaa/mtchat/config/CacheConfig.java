package com.mtlaa.mtchat.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 配置caffeine本地缓存
 */
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    @Override
    @Bean(value = "msgCacheManager")
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000));
        return manager;
    }
}

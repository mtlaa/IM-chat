package com.mtlaa.mychat.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * 配置缓存的底层实现，使用Caffeine本地缓存
 * 后续可以使用spring的注解使用缓存
 */
@EnableCaching
@Configuration
public class CacheConfig extends CachingConfigurerSupport {

    @Bean("caffeineCacheManager")
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 方案一(常用)：定制化缓存Cache
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(200));
        return cacheManager;
    }

}

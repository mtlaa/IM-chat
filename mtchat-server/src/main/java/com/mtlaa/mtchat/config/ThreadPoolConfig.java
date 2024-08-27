package com.mtlaa.mtchat.config;


import com.mtlaa.mtchat.config.properties.CommonThreadPoolProperties;
import com.mtlaa.mtchat.config.properties.PushThreadPoolProperties;
import com.mtlaa.mtchat.utils.MyThreadFactory;
import com.mtlaa.mychat.transaction.annotation.SecureInvokeConfigurer;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 统一管理项目线程池。注意：只有通用线程池使用了 @Primary 注解，这样在项目中需要线程池的地方都会使用该线程池（按类型自动注入时）
 */
@Configuration
@EnableConfigurationProperties({CommonThreadPoolProperties.class, PushThreadPoolProperties.class})
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer, SecureInvokeConfigurer {
    private static Executor mtchatExecutor;

    public static final String MTCHAT_EXECUTOR = "mtchatExecutor";
    public static final String WEBSOCKET_PUSH_EXECUTOR = "websocketPushExecutor";
    /**
     * 通过实现 AsyncConfigurer 的 getAsyncExecutor()方法，管理 @Async 注解的异步方法使用的线程池。
     * @return 异步线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        return mtchatExecutor;
    }

    /**
     * 返回本地消息表事务框架需要的线程池
     */
    @Override
    public Executor getSecureInvokeExecutor() {
        return mtchatExecutor;
    }

    /**
     * 项目通用线程池。使用 @Primary 注解，这样在注入线程池的地方会优先注入该线程池（该注解是提高bean的优先级）
     * @return 项目的统一线程池
     */
    @Bean(MTCHAT_EXECUTOR)
    @Primary
    public ThreadPoolTaskExecutor mtchatExecutor(CommonThreadPoolProperties properties){
        ThreadPoolTaskExecutor executor = getThreadPoolTaskExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getQueueCapacity(),
                properties.getThreadNamePrefix(),
                new ThreadPoolExecutor.CallerRunsPolicy());

        mtchatExecutor = executor;
        return executor;
    }

    /**
     * websocket 推送线程池
     * @param properties 配置信息
     * @return 推送线程池
     */
    @Bean(WEBSOCKET_PUSH_EXECUTOR)
    public ThreadPoolTaskExecutor websocketPushExecutor(PushThreadPoolProperties properties){
        return getThreadPoolTaskExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getQueueCapacity(),
                properties.getThreadNamePrefix(),
                new ThreadPoolExecutor.AbortPolicy());
    }


    /**
     * 创建Spring的线程池（因为其提供了优雅停机的功能）
     */
    @NotNull
    private ThreadPoolTaskExecutor getThreadPoolTaskExecutor(Integer corePoolSize,
                                                             Integer maxPoolSize,
                                                             Integer queueCapacity,
                                                             String threadNamePrefix,
                                                             RejectedExecutionHandler reject) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setRejectedExecutionHandler(reject);
        // 替换线程工厂，在里面设置线程的未捕获异常处理器
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }

}

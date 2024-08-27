package com.mtlaa.mtchat.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mtchat.thread-pool.websocket-push")
public class PushThreadPoolProperties {
    private Integer corePoolSize;
    private Integer maxPoolSize;
    private Integer queueCapacity;
    private String threadNamePrefix;
}

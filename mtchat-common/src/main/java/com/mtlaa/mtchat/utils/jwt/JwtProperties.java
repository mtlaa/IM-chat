package com.mtlaa.mtchat.utils.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Create 2023/12/6 19:25
 */
@Data
@Component
@ConfigurationProperties(prefix = "mtchat.jwt")
public class JwtProperties {
    private String secretKey;
    private Long ttl;
}

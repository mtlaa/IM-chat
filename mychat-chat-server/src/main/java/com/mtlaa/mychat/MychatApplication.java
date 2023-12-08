package com.mtlaa.mychat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Create 2023/11/30 11:30
 */
@SpringBootApplication(scanBasePackages = {"com.mtlaa.mychat"})
@EnableTransactionManagement
public class MychatApplication {
    public static void main(String[] args) {
        SpringApplication.run(MychatApplication.class, args);
    }
}

package com.mtlaa.mychat.common.config;

import com.mtlaa.mychat.common.interceptor.CollectorInterceptor;
import com.mtlaa.mychat.common.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Create 2023/12/11 16:21
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Autowired
    private JwtInterceptor jwtInterceptor;
    @Autowired
    private CollectorInterceptor collectorInterceptor;

    /**
     * 注册自定义拦截器
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/capi/**");
        registry.addInterceptor(collectorInterceptor)
                .addPathPatterns("/capi/**");
    }
}

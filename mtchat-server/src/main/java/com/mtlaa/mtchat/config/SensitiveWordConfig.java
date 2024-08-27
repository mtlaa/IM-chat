package com.mtlaa.mtchat.config;

import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.sensitive.dao.SensitiveWordDao;
import com.mtlaa.mtchat.sensitive.domain.SensitiveWord;
import com.mtlaa.mtchat.utils.sensitive.SensitiveWordFilter;
import com.mtlaa.mtchat.utils.sensitive.SensitiveWordProperties;
import com.mtlaa.mtchat.utils.sensitive.impl.ACFilter;
import com.mtlaa.mtchat.utils.sensitive.impl.DFAFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(SensitiveWordProperties.class)
@Slf4j
public class SensitiveWordConfig {
    @Autowired
    private SensitiveWordDao sensitiveWordDao;

    @Bean
    public SensitiveWordFilter sensitiveWordFilter(SensitiveWordProperties properties){
        SensitiveWordFilter filter;
        switch (properties.getType()){
            case DFA:
                filter = new DFAFilter();
                break;
            case AC:
                filter = new ACFilter();
                break;
            default:
                throw new BusinessException("敏感词过滤器类型配置错误");
        }
        List<String> collect = sensitiveWordDao.list().stream().map(SensitiveWord::getWord).collect(Collectors.toList());
//        long start = System.currentTimeMillis();
        filter.loadWord(collect);
//        long end = System.currentTimeMillis();
//        log.info("-------------加载耗时：{}ms--------------", end-start);
        return filter;
    }
    /**
     * 加载性能：
     * dfa: 25, 25, 25 ms
     * ac:  43, 40, 41 ms
     */
    /**
     * 过滤平均耗时:
     * dfa: 97 ms
     * ac:  71 ms
     */
}

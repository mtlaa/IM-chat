package com.mtlaa.mychat.common.config;

import com.mtlaa.mychat.common.utils.sensitiveWord.DFAFilter;
import com.mtlaa.mychat.common.utils.sensitiveWord.IWordFactory;
import com.mtlaa.mychat.common.utils.sensitiveWord.SensitiveWordBs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Create 2024/1/7 13:44
 */
@Configuration
public class SensitiveWordConfig {
    @Autowired
    private IWordFactory wordFactory;

    @Bean
    public SensitiveWordBs sensitiveWordBs(){
        return SensitiveWordBs.newInstance()
                .sensitiveWord(wordFactory)
                .filterStrategy(DFAFilter.getInstance())
                .init();
    }
}

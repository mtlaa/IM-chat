package com.mtlaa.mychat.transaction.config;

import com.mtlaa.mychat.transaction.annotation.SecureInvokeConfigurer;
import com.mtlaa.mychat.transaction.aspect.SecureInvokeAspect;
import com.mtlaa.mychat.transaction.dao.SecureInvokeRecordDao;
import com.mtlaa.mychat.transaction.mapper.SecureInvokeRecordMapper;
import com.mtlaa.mychat.transaction.service.MQProducer;
import com.mtlaa.mychat.transaction.service.SecureInvokeService;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.util.function.SingletonSupplier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-06
 */
@Configuration
@EnableScheduling
@MapperScan(basePackageClasses = SecureInvokeRecordMapper.class)
@Import({SecureInvokeAspect.class, SecureInvokeRecordDao.class})
@DependsOn("mtchatExecutor")    // 该自动配置类需要设置线程池。
                                // 需要确保在该配置类bean的初始化之前我们项目的自定义统一线程池的bean已经被创建
                                // 使用该注解来设置bean的初始化顺序（@Order用于设置切面的顺序和注入集合的bean时集合中bean的顺序，
                                // 对bean的初始化顺序不起作用；@AutoConfigurerOrder用于设置外部包之间自动配置类的初始化顺序，对于内部bean不起作用）
public class TransactionAutoConfiguration {

    @Nullable
    protected Executor executor;

    /**
     * Collect any {@link AsyncConfigurer} beans through autowiring.
     */
    @Autowired
    void setConfigurers(ObjectProvider<SecureInvokeConfigurer> configurers) {
        Supplier<SecureInvokeConfigurer> configurer = SingletonSupplier.of(() -> {
            List<SecureInvokeConfigurer> candidates = configurers.stream().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(candidates)) {
                return null;
            }
            if (candidates.size() > 1) {
                throw new IllegalStateException("Only one SecureInvokeConfigurer may exist");
            }
            return candidates.get(0);
        });
        executor = Optional.ofNullable(configurer.get()).map(SecureInvokeConfigurer::getSecureInvokeExecutor).orElse(ForkJoinPool.commonPool());
    }

    @Bean
    public SecureInvokeService getSecureInvokeService(SecureInvokeRecordDao dao) {
        return new SecureInvokeService(dao, executor);
    }

    @Bean
    public MQProducer getMQProducer() {
        return new MQProducer();
    }
}

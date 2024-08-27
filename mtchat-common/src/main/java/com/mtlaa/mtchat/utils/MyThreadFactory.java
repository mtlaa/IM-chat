package com.mtlaa.mtchat.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ThreadFactory;

@Slf4j
@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {
    private ThreadFactory factory;

    /**
     * 装饰器模式。调用Spring线程工厂创建线程，我们再设置线程的异常捕获
     * @param r a runnable to be executed by new thread instance
     * @return 线程
     */
    @Override
    public Thread newThread(@NotNull Runnable r) {
        Thread thread = factory.newThread(r);   // 调用Spring的线程工厂，里面有设置线程组、优先级、线程名、是否守护
        // 装饰器的新增操作：设置线程的未捕获异常处理器
        thread.setUncaughtExceptionHandler((t, e)->{
            log.error("Exception in thread '{}' ", t.getName(), e);
        });
        return thread;
    }
}

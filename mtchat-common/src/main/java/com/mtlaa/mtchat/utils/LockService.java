package com.mtlaa.mtchat.utils;

import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.exception.CommonErrorEnum;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁的编程实现
 */
@Service
public class LockService {
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 用于编程式调用，加锁时不等待，加锁失败则异常。会抛出异常
     * @param key 锁的key
     * @param supplier 需要加锁的代码逻辑
     * @return 原方法返回值
     * @param <T> 返回值类型
     * @throws Throwable InterruptedException 和 被加锁代码运行时异常 以及 BusinessException(CommonErrorEnum.LOCK_LIMIT)
     */
    public <T> T executeWithLock(String key, Supplier<T> supplier) throws Throwable {
        return executeWithLockThrows(key, -1, TimeUnit.MILLISECONDS, supplier::get);
    }

    /**
     * 用于编程式调用，加锁时会等待指定时间。会抛出异常
     * @param key 锁的key
     * @param waitTime 加锁失败时的等待时间
     * @param unit 时间单位
     * @param supplier 需要加锁的代码逻辑
     * @return 原方法返回值
     * @param <T> 返回值类型
     * @throws Throwable InterruptedException 和 被加锁代码运行时异常 以及 BusinessException(CommonErrorEnum.LOCK_LIMIT)
     */
    public <T> T executeWithLock(String key, int waitTime, TimeUnit unit, Supplier<T> supplier) throws Throwable {
        return executeWithLockThrows(key, waitTime, unit, supplier::get);
    }

    /**
     * 用于注解中切面的调用
     * @param key 锁的key
     * @param waitTime 加锁失败时的等待时间
     * @param unit 时间单位
     * @param supplier 需要加锁的代码逻辑
     * @return 原方法返回值
     * @param <T> 返回值类型
     * @throws Throwable InterruptedException 和 被加锁代码运行时异常 以及 BusinessException(CommonErrorEnum.LOCK_LIMIT)
     */
    public <T> T  executeWithLockThrows(String key, int waitTime, TimeUnit unit, SupplierThrow<T> supplier) throws Throwable {
        RLock lock = redissonClient.getLock(key);
        boolean locked = lock.tryLock(waitTime, unit);
        if (!locked){
            throw new BusinessException(CommonErrorEnum.LOCK_LIMIT);
        }
        try {
            return supplier.get();
        } finally {
            if (lock.isLocked())
                lock.unlock();
        }
    }

    /**
     * 可以抛出异常的 Supplier
     * @param <T>
     */
    @FunctionalInterface
    public interface SupplierThrow<T>{
        T get() throws Throwable;
    }
}

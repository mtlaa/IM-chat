package com.mtlaa.mychat.common.service;

import com.mtlaa.mychat.common.exception.BusinessException;
import com.mtlaa.mychat.common.exception.CommonErrorEnum;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Create 2023/12/13 11:22
 */
@Service
@Slf4j
public class LockService {
    @Autowired
    private RedissonClient redissonClient;


    public <T> T executeWithLockThrows(String key, int time, TimeUnit unit, SupplierThrow<T> supplier) throws Throwable {
        RLock lock = redissonClient.getLock(key);
        boolean success = lock.tryLock(time, unit);
        if(!success){
            throw new BusinessException(CommonErrorEnum.LOCK_LIMIT);
        }
        try{
            return supplier.get();
        } finally {
            if(lock.isLocked() && lock.isHeldByCurrentThread())
                lock.unlock();
        }
    }



    public <T> T executeWithLock(String key, int time, TimeUnit unit, Supplier<T> supplier) throws Throwable {
        return executeWithLockThrows(key, time, unit, supplier::get);
    }

    public <T> T executeWithLock(String key, Supplier<T> supplier) throws Throwable {
        return executeWithLock(key, -1, TimeUnit.MILLISECONDS, supplier);
    }

    @FunctionalInterface
    public interface SupplierThrow<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws Throwable;
    }
}

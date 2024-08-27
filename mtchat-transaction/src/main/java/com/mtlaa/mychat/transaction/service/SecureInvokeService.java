package com.mtlaa.mychat.transaction.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.mtlaa.mtchat.utils.redis.JsonUtils;
import com.mtlaa.mychat.transaction.dao.SecureInvokeRecordDao;
import com.mtlaa.mychat.transaction.domain.dto.SecureInvokeDTO;
import com.mtlaa.mychat.transaction.domain.entity.SecureInvokeRecord;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Description: 安全执行处理器
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-20
 */
@Slf4j
@AllArgsConstructor
public class SecureInvokeService {

    public static final double RETRY_INTERVAL_MINUTES = 2D;

    private final SecureInvokeRecordDao secureInvokeRecordDao;

    /**
     * 项目的统一通用线程池
     */
    private final Executor executor;

    /**
     * Spring的定时任务，每5秒自动执行一次
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void retry() {
        log.info("本地消息表，自动执行确保最终一致性...");
        // 查询出需要重试的记录
        List<SecureInvokeRecord> secureInvokeRecords = secureInvokeRecordDao.getWaitRetryRecords();
        for (SecureInvokeRecord secureInvokeRecord : secureInvokeRecords) {
            doAsyncInvoke(secureInvokeRecord);
        }
    }

    /**
     * 当第一次执行时，把执行记录入库
     * @param record 方法执行记录
     */
    public void save(SecureInvokeRecord record) {
        secureInvokeRecordDao.save(record);
    }

    /**
     * 执行失败时，更新记录，更新重试的次数以及下一次重试的时间
     * @param record 方法执行记录
     * @param errorMsg 执行失败时的错误消息
     */
    private void retryRecord(SecureInvokeRecord record, String errorMsg) {
        Integer retryTimes = record.getRetryTimes() + 1;
        SecureInvokeRecord update = new SecureInvokeRecord();
        update.setId(record.getId());
        update.setFailReason(errorMsg);
        update.setNextRetryTime(getNextRetryTime(retryTimes));
        if (retryTimes > record.getMaxRetryTimes()) {
            update.setStatus(SecureInvokeRecord.STATUS_FAIL);  // 如果超过最大重试次数，标记为失败，等待人工处理
        } else {
            update.setRetryTimes(retryTimes);
        }
        secureInvokeRecordDao.updateById(update);  // 更新记录，等待下一次重试
    }

    /**
     * 获取下一次重试的时间。采用指数上升算法 或者 类似于TCP重传中每次时间为上次的2倍
     * @param retryTimes 重试的次数
     * @return 下次重试的时间
     */
    private Date getNextRetryTime(Integer retryTimes) {//或者可以采用退避算法
        double waitMinutes = Math.pow(RETRY_INTERVAL_MINUTES, retryTimes);//重试时间指数上升 2m 4m 8m 16m
        return DateUtil.offsetMinute(new Date(), (int) waitMinutes);
    }

    /**
     * 执行成功删除记录
     * @param id 记录id
     */
    private void removeRecord(Long id) {
        secureInvokeRecordDao.removeById(id);
    }

    public void invoke(SecureInvokeRecord record, boolean async) {
        // 只有第一次执行时进入该方法，后续重试会直接doInvoke
        // 判断事务状态在切面中已经判断了，这里再判断其实是多余的
        boolean inTransaction = TransactionSynchronizationManager.isActualTransactionActive();
        //非事务状态，直接执行，不做任何保证。
        if (!inTransaction) {
            return;
        }
        //保存执行数据
        save(record);  // 直接入库了
        // TransactionSynchronizationManager为本次事务的管理器
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @SneakyThrows
            @Override
            public void afterCommit() {
                //事务后执行
                if (async) {
                    doAsyncInvoke(record); // 异步执行，开新线程
                } else {
                    doInvoke(record); // 同步执行，继续在事务线程执行
                }
            }
        });
    }

    /**
     * 异步执行方法
     * @param record 方法记录
     */
    public void doAsyncInvoke(SecureInvokeRecord record) {
        executor.execute(() -> {
            System.out.println(Thread.currentThread().getName());
            doInvoke(record);
        });
    }

    /**
     * 同步执行record中记录的方法
     * @param record 方法执行记录
     */
    public void doInvoke(SecureInvokeRecord record) {
        SecureInvokeDTO secureInvokeDTO = record.getSecureInvokeDTO();  // 方法快照
        try {
            SecureInvokeHolder.setInvoking();  // 设置一个标志，表明本线程已经执行过record中的方法（不在事务内）
            Class<?> beanClass = Class.forName(secureInvokeDTO.getClassName());
            Object bean = SpringUtil.getBean(beanClass); // 通过Spring获取目标方法所在的bean，获取的是代理对象，
                                                         // 因此通过该对象使用反射执行方法也会进入到切面
            List<String> parameterStrings = JsonUtils.toList(secureInvokeDTO.getParameterTypes(), String.class);
            List<Class<?>> parameterClasses = getParameters(parameterStrings);
            Method method = ReflectUtil.getMethod(beanClass, secureInvokeDTO.getMethodName(), parameterClasses.toArray(new Class[]{}));
            Object[] args = getArgs(secureInvokeDTO, parameterClasses);
            //执行方法，会进入到 SecureInvokeAspect 切面。（由于当前线程设置了一个状态，进入到切面后会直接执行原本方法，不会再次入库执行记录）
            method.invoke(bean, args);
            //执行成功更新状态，即这条record没用了，直接删除
            removeRecord(record.getId());
        } catch (Throwable e) {
            log.error("SecureInvokeService invoke fail", e);
            //执行失败，等待下次执行
            retryRecord(record, e.getMessage());
        } finally {
            // 清除设置的标志，避免内存泄漏
            SecureInvokeHolder.invoked();
        }
    }

    /**
     * 使用 jackson 把json字符串格式的的参数值转换为JsonNode，然后再根据参数类型把JsonNode转为对象
     * @param secureInvokeDTO 包含参数值（json字符串）
     * @param parameterClasses 参数的类型
     * @return 参数对象数组
     */
    @NotNull
    private Object[] getArgs(SecureInvokeDTO secureInvokeDTO, List<Class<?>> parameterClasses) {
        JsonNode jsonNode = JsonUtils.toJsonNode(secureInvokeDTO.getArgs());
        Object[] args = new Object[jsonNode.size()];
        for (int i = 0; i < jsonNode.size(); i++) {
            Class<?> aClass = parameterClasses.get(i);
            args[i] = JsonUtils.nodeToValue(jsonNode.get(i), aClass);
        }
        return args;
    }

    /**
     * 使用 Class 类获取参数类型字符串名对应的Class对象
     * @param parameterStrings 类型名
     * @return clazz对象
     */
    @NotNull
    private List<Class<?>> getParameters(List<String> parameterStrings) {
        return parameterStrings.stream().map(name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                log.error("SecureInvokeService class not fund", e);
            }
            return null;
        }).collect(Collectors.toList());
    }
}

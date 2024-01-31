package com.mtlaa.mychat.transaction.service;

import java.util.Objects;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-10-02
 */
public class SecureInvokeHolder {
    private static final ThreadLocal<Boolean> INVOKE_THREAD_LOCAL = new ThreadLocal<>();

    public static boolean isInvoking() {
        return Objects.nonNull(INVOKE_THREAD_LOCAL.get());
    }

    /**
     * 设置一个标志，表明本线程已经执行过record中的方法
     * 只要执行record记录的方法，就会被切面增强，该标志位主要是为了能够在record入库后的立即执行（第一次执行）时跳过切面的增强
     * - 如果是重试，是由自动任务调用record中的方法，肯定不是在事务内，
     *                                      TransactionSynchronizationManager.isActualTransactionActive() == false
     * - 如果是在事务内的第一次调用，上面那个条件返回的是true，为了避免在切面中重复把record入库，增加此标志位，用于表明不在事务内
     */
    public static void setInvoking() {
        INVOKE_THREAD_LOCAL.set(Boolean.TRUE);
    }

    public static void invoked() {
        INVOKE_THREAD_LOCAL.remove();
    }
}

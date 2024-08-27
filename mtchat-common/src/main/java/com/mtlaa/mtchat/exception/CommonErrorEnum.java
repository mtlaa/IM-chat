package com.mtlaa.mtchat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create 2023/12/11 20:20
 */
@AllArgsConstructor
@Getter
public enum CommonErrorEnum implements ErrorEnum {

    SYSTEM_ERROR(-1, "系统出小差了，请稍后再试哦~~"),
    PARAM_VALID(-2, "参数校验失败"),
    FREQUENCY_LIMIT(-3, "限流，请求太频繁了，请稍后再试哦~~"),
    LOCK_LIMIT(-4, "分布式锁，请求太频繁了，请稍后再试哦~~"),
    ;
    private final Integer code;
    private final String msg;

    @Override
    public Integer getErrorCode() {
        return this.code;
    }

    @Override
    public String getErrorMsg() {
        return this.msg;
    }
}

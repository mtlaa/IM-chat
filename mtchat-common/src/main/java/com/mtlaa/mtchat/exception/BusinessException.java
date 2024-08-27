package com.mtlaa.mtchat.exception;

import lombok.Data;

/**
 * Create 2023/12/12 9:25
 */
@Data
public class BusinessException extends RuntimeException{
    protected Integer errCode;
    protected String errMsg;
    public BusinessException(String errMsg){
        super(errMsg);
        this.errCode = 0;
        this.errMsg = errMsg;
    }
    public BusinessException(Integer errCode, String errMsg){
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public BusinessException(CommonErrorEnum commonErrorEnum) {
        this(commonErrorEnum.getErrorCode(), commonErrorEnum.getErrorMsg());
    }
}

package com.mtlaa.mtchat.exception;

import com.mtlaa.mtchat.domain.common.vo.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Create 2023/12/11 19:56
 * 全局异常处理类
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 捕获方法参数invalid异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> doException(MethodArgumentNotValidException ex){
        StringBuilder stringBuilder = new StringBuilder();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> stringBuilder.append(err.getField())
                        .append(" ").append(err.getDefaultMessage()).append('\n'));
        String errorMessage = stringBuilder.toString();
        log.error("参数非法异常：{}", errorMessage);
        return ApiResult.fail(CommonErrorEnum.PARAM_VALID.getCode(), errorMessage);
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResult<?> businessException(BusinessException bex){
        log.error("业务异常：{}", bex.getErrMsg());
        return ApiResult.fail(bex.getErrCode(), bex.getErrMsg());
    }

    @ExceptionHandler(Throwable.class)
    public ApiResult<?> systemException(Throwable ex){
        log.error("捕获系统异常：", ex);
        return ApiResult.fail(CommonErrorEnum.SYSTEM_ERROR.getCode(), CommonErrorEnum.SYSTEM_ERROR.getMsg());
    }
}

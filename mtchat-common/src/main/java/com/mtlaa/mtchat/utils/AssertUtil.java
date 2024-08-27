package com.mtlaa.mtchat.utils;


import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.exception.CommonErrorEnum;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Iterator;
import java.util.Set;

/**
 * Create 2023/12/25 15:50
 */
public class AssertUtil {
    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    /**
     * 注解验证参数(全部校验,抛出异常)
     *
     * @param obj
     */
    public static <T> void allCheckValidateThrow(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);
        if (constraintViolations.size() > 0) {
            StringBuilder errorMsg = new StringBuilder();
            Iterator<ConstraintViolation<T>> iterator = constraintViolations.iterator();
            while (iterator.hasNext()) {
                ConstraintViolation<T> violation = iterator.next();
                //拼接异常信息
                errorMsg.append(violation.getPropertyPath().toString()).append(":").append(violation.getMessage()).append(",");
            }
            //去掉最后一个逗号
            throw new BusinessException(CommonErrorEnum.PARAM_VALID.getErrorCode(), errorMsg.toString().substring(0, errorMsg.length() - 1));
        }
    }
}

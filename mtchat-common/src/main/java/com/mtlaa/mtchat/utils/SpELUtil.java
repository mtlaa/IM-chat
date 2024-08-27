package com.mtlaa.mtchat.utils;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class SpELUtil {
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 获取方法的全限定名：类全名 # 方法名
     * @param method 方法
     * @return 全限定名
     */
    public static String getMethodName(Method method){
        return method.getDeclaringClass() + "#" + method.getName();
    }

    /**
     * 解析 SpEL 表达式
     * @param method 方法
     * @param spEL SpEL表达式
     * @param args 调用方法的参数
     * @return 解析的值
     */
    public static String parseSpEL(Method method, String spEL, Object[] args) {
        // 获取参数名
        String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
        // 创建解析需要的上下文
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < args.length && parameterNames != null; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        // 使用解析器创建表达式
        Expression expression = PARSER.parseExpression(spEL);
        // 使用表达式传入上下文，获得解析结果
        return expression.getValue(context, String.class);
    }
}

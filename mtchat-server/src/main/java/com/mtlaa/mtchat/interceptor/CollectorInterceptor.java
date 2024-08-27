package com.mtlaa.mtchat.interceptor;

import cn.hutool.extra.servlet.ServletUtil;

import com.mtlaa.mtchat.domain.common.entity.RequestInfo;
import com.mtlaa.mtchat.utils.RequestHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create 2023/12/11 17:39
 * 收集请求上下文的拦截器，把这次请求的uid和ip保存起来
 * 后续可以方便使用
 */
@Component
public class CollectorInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        Long uid = (Long) request.getAttribute(JwtInterceptor.UID);
        String ip = ServletUtil.getClientIP(request);
        RequestHolder.set(new RequestInfo(uid, ip));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestHolder.remove();
    }
}

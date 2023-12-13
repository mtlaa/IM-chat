package com.mtlaa.mychat.common.interceptor;


import com.mtlaa.mychat.common.domain.dto.RequestInfo;
import com.mtlaa.mychat.common.exception.HttpErrorEnum;
import com.mtlaa.mychat.common.utils.RequestHolder;
import com.mtlaa.mychat.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Create 2023/12/11 16:13
 * 登录拦截器
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    private static final String HEADER_TOKEN = "Authorization";
    private static final String START_TOKEN = "Bearer ";
    public static final String UID = "uid";

    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        String token = getJwt(request);
        Long uid = loginService.getValidUid(token);
        if(uid != null){
            // 校验成功，保存到HttpServletRequest
            request.setAttribute(UID, uid);
        }else{
            // 校验失败，判断是否是public请求
            String[] split = request.getRequestURI().split("/");
            boolean isPublic = split.length > 3 && split[3].equals("public");
            if(!isPublic) {
                // 不是public，返回401的错误消息
                HttpErrorEnum.ACCESS_DENIED.sendHttpError(response);
                return false;
            }
        }
        return true;
    }

    private String getJwt(HttpServletRequest request) {
        String token = request.getHeader(HEADER_TOKEN);
        return Optional.ofNullable(token)
                .filter(s -> s.startsWith(START_TOKEN))
                .map(s -> s.replace(START_TOKEN, ""))
                .orElse(null);
    }
}

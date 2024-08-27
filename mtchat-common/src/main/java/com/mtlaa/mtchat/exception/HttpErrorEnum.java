package com.mtlaa.mtchat.exception;

import cn.hutool.http.ContentType;
import com.mtlaa.mtchat.domain.common.vo.response.ApiResult;
import com.mtlaa.mtchat.utils.redis.JsonUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.io.Charsets;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Create 2023/12/11 16:49
 */
@AllArgsConstructor
public enum HttpErrorEnum {
    ACCESS_DENIED(401, "登录失效，请重新登录");

    private final Integer errCode;
    private final String desc;

    public void sendHttpError(HttpServletResponse response) throws IOException {
        response.setStatus(errCode);
        response.setContentType(ContentType.JSON.toString(Charsets.UTF_8));
        response.getWriter().write(JsonUtils.toStr(ApiResult.fail(errCode, desc)));
    }
}

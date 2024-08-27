package com.mtlaa.mtchat.domain.websocket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create 2023/11/30 15:27
 */
@AllArgsConstructor
@Getter
public enum WebSocketRequestTypeEnum {
    LOGIN(1, "请求登录二维码"),
    HEARTBEAT(2, "心跳包"),
    AUTHORIZE(3, "登录认证");
    private final Integer type;
    private final String desc;

    private static Map<Integer, WebSocketRequestTypeEnum> cache;
    static {
        cache = Arrays.stream(WebSocketRequestTypeEnum.values()).collect(Collectors.toMap(WebSocketRequestTypeEnum::getType, Function.identity()));
    }
    public static WebSocketRequestTypeEnum of(Integer type) {
        return cache.get(type);
    }
}

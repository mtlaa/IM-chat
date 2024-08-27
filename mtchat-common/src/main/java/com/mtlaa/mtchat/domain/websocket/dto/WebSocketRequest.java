package com.mtlaa.mtchat.domain.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create 2023/11/30 15:23
 * 基本的websocket请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketRequest {
    /**
     * @see com.mtlaa.mtchat.domain.websocket.enums.WebSocketRequestTypeEnum
     */
    private Integer type;
    // 前端请求传递的数据是json字符串
    private String data;
}

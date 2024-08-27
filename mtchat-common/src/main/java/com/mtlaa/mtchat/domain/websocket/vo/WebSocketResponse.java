package com.mtlaa.mtchat.domain.websocket.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create 2023/11/30 15:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketResponse<T> {
    /**
     * @see com.mtlaa.mtchat.domain.websocket.enums.WebSocketResponseTypeEnum
     */
    private Integer type;
    private T data;
}

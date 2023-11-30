package com.mtlaa.mychat.websocket.domain.vo;

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
     * @see com.mtlaa.mychat.websocket.domain.enums.WebSocketResponseTypeEnum
     */
    private Integer type;
    private T data;
}

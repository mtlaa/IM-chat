package com.mtlaa.mychat.chat.domain.vo.response.wsMsg;

import lombok.Data;

/**
 * Description: ws的基本返回信息体
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-19
 */
@Data
public class WSBaseResp<T> {
    /**
     * ws推送给前端的消息
     *
     * @see com.mtlaa.mychat.websocket.domain.enums.WebSocketResponseTypeEnum
     */
    private Integer type;
    private T data;
}

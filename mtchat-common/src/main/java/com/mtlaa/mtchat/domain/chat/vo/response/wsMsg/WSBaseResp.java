package com.mtlaa.mtchat.domain.chat.vo.response.wsMsg;

import com.mtlaa.mtchat.domain.websocket.enums.WebSocketResponseTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: ws的基本返回信息体
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSBaseResp<T> {
    /**
     * ws推送给前端的消息
     *
     * @see WebSocketResponseTypeEnum
     */
    private Integer type;
    private T data;

}

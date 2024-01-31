package com.mtlaa.mychat.websocket.service;

import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.websocket.domain.vo.WebSocketResponse;
import io.netty.channel.Channel;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;

/**
 * Create 2023/12/6 10:45
 */
@Service
public interface WebSocketService {
    void connect(Channel channel);

    void handleLoginRequest(Channel channel) throws WxErrorException;

    void disconnect(Channel channel);

    void loginSuccess(Integer code, User user);

    void waitAuthorizeMsg(Integer code);

    void handleAuthorizeJwt(Channel channel, String token);

    void sendMsgToAll(WebSocketResponse<?> msg);

    void sendMsgToUid(WebSocketResponse<?> webSocketResponse, Long uid);
}

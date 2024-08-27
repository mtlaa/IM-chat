package com.mtlaa.mtchat.websocket.service;


import com.mtlaa.mtchat.domain.chat.vo.response.wsMsg.WSBaseResp;
import com.mtlaa.mtchat.domain.user.entity.User;
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

    void sendWaitAuthorizeMsg(Integer code);

    void handleAuthorizeJwt(Channel channel, String token);

    void sendMsgToAll(WSBaseResp<?> msg);

    void sendMsgToUid(WSBaseResp<?> webSocketResponse, Long uid);
}

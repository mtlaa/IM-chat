package com.mtlaa.mychat.websocket.service.adapter;

import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.websocket.domain.enums.WebSocketResponseTypeEnum;
import com.mtlaa.mychat.websocket.domain.vo.WSLoginSuccess;
import com.mtlaa.mychat.websocket.domain.vo.WSLoginUrl;
import com.mtlaa.mychat.websocket.domain.vo.WebSocketResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;

/**
 * Create 2023/12/6 14:41
 */
@Data
public class WebSocketAdapter {
    public static WebSocketResponse<WSLoginUrl> build(WxMpQrCodeTicket wxMpQrCodeTicket){
        WebSocketResponse<WSLoginUrl> response = new WebSocketResponse<>();
        response.setType(WebSocketResponseTypeEnum.LOGIN_URL.getType());
        response.setData(new WSLoginUrl(wxMpQrCodeTicket.getUrl()));
        return response;
    }
    public static WebSocketResponse<WSLoginSuccess> build(User user, String token){
        WebSocketResponse<WSLoginSuccess> response = new WebSocketResponse<>();
        response.setType(WebSocketResponseTypeEnum.LOGIN_SUCCESS.getType());

        WSLoginSuccess data = WSLoginSuccess.builder()
                .uid(user.getId())
                .avatar(user.getAvatar())
                .name(user.getName())
                .power(0)   // TODO 是否为管理账户
                .token(token)
                .build();
        response.setData(data);
        return response;
    }

    public static WebSocketResponse buildInvalidToken(){
        WebSocketResponse response = new WebSocketResponse<>();
        response.setType(WebSocketResponseTypeEnum.INVALIDATE_TOKEN.getType());
        return response;
    }
}

package com.mtlaa.mtchat.websocket.service.adapter;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.mtlaa.mtchat.cache.user.UserCache;
import com.mtlaa.mtchat.domain.chat.dto.ChatMsgRecallDTO;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMemberResp;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMessageResp;
import com.mtlaa.mtchat.domain.chat.vo.response.wsMsg.WSBaseResp;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.enums.UserActiveStatusEnum;
import com.mtlaa.mtchat.domain.websocket.enums.WebSocketResponseTypeEnum;
import com.mtlaa.mtchat.domain.websocket.vo.*;
import lombok.Data;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.Date;

/**
 * Create 2023/12/6 14:41
 */
@Data
public class WebSocketAdapter {

    public static WSBaseResp<WSLoginUrl> build(WxMpQrCodeTicket wxMpQrCodeTicket){
        WSBaseResp<WSLoginUrl> response = new WSBaseResp<>();
        response.setType(WebSocketResponseTypeEnum.LOGIN_URL.getType());
        response.setData(new WSLoginUrl(wxMpQrCodeTicket.getUrl()));
        return response;
    }
    public static WSBaseResp<WSLoginSuccess> build(User user, String token, boolean hasPower){
        WSBaseResp<WSLoginSuccess> response = new WSBaseResp<>();
        response.setType(WebSocketResponseTypeEnum.LOGIN_SUCCESS.getType());

        WSLoginSuccess data = WSLoginSuccess.builder()
                .uid(user.getId())
                .avatar(user.getAvatar())
                .name(user.getName())
                .power(hasPower ? 1 : 0)   // 是否为管理账户
                .token(token)
                .build();
        response.setData(data);
        return response;
    }

    /**
     * 构建token失效的 websocket 消息
     * @return websocket响应
     */
    public static WSBaseResp<?> buildInvalidToken(){
        WSBaseResp<?> response = new WSBaseResp<>();
        response.setType(WebSocketResponseTypeEnum.INVALIDATE_TOKEN.getType());
        return response;
    }

    public static WSBaseResp<ChatMessageResp> buildMsgSend(ChatMessageResp msgResp) {
        WSBaseResp<ChatMessageResp> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WebSocketResponseTypeEnum.MESSAGE.getType());
        wsBaseResp.setData(msgResp);
        return wsBaseResp;
    }

    public static WSBaseResp<?> buildMsgRecall(ChatMsgRecallDTO recallDTO) {
        WSBaseResp<WSMsgRecall> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WebSocketResponseTypeEnum.RECALL.getType());
        WSMsgRecall recall = new WSMsgRecall();
        BeanUtils.copyProperties(recallDTO, recall);
        wsBaseResp.setData(recall);
        return wsBaseResp;
    }

    public static WSBaseResp<?> buildApplySend(WSFriendApply wsFriendApply){
        WSBaseResp<WSFriendApply> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WebSocketResponseTypeEnum.APPLY.getType());
        wsBaseResp.setData(wsFriendApply);
        return wsBaseResp;
    }

    public static WSBaseResp<?> buildOnlineNotify(User user) {
        WSBaseResp<WSOnlineOfflineNotify> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WebSocketResponseTypeEnum.ONLINE_OFFLINE_NOTIFY.getType());
        WSOnlineOfflineNotify wsOnlineOfflineNotify = new WSOnlineOfflineNotify();
        wsOnlineOfflineNotify.setChangeList(Collections.singletonList(buildOnlineInfo(user)));
        // 设置在线人数 该在线人数会显示在群聊的在线人数上
        setOnlineNumber(wsOnlineOfflineNotify);

        wsBaseResp.setData(wsOnlineOfflineNotify);
        return wsBaseResp;
    }

    private static void setOnlineNumber(WSOnlineOfflineNotify wsOnlineOfflineNotify) {
        UserCache userCache = SpringUtil.getBean(UserCache.class);
        wsOnlineOfflineNotify.setOnlineNum(userCache.getOnlineNum());
    }

    public static WSBaseResp<?> buildOfflineNotify(User user) {
        WSBaseResp<WSOnlineOfflineNotify> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WebSocketResponseTypeEnum.ONLINE_OFFLINE_NOTIFY.getType());
        WSOnlineOfflineNotify wsOnlineOfflineNotify = new WSOnlineOfflineNotify();
        wsOnlineOfflineNotify.setChangeList(Collections.singletonList(buildOfflineInfo(user)));
        // 设置在线人数
        setOnlineNumber(wsOnlineOfflineNotify);

        wsBaseResp.setData(wsOnlineOfflineNotify);
        return wsBaseResp;
    }

    private static ChatMemberResp buildOnlineInfo(User user) {
        ChatMemberResp info = new ChatMemberResp();
        BeanUtil.copyProperties(user, info);
        info.setUid(user.getId());
        info.setActiveStatus(UserActiveStatusEnum.ONLINE.getStatus());
        info.setLastOptTime(new Date());
        return info;
    }
    private static ChatMemberResp buildOfflineInfo(User user) {
        ChatMemberResp info = new ChatMemberResp();
        BeanUtil.copyProperties(user, info);
        info.setUid(user.getId());
        info.setActiveStatus(UserActiveStatusEnum.OFFLINE.getStatus());
        info.setLastOptTime(new Date());
        return info;
    }
}

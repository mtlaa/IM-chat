package com.mtlaa.mtchat.event.listener;

import com.mtlaa.mtchat.cache.chat.GroupMemberCache;
import com.mtlaa.mtchat.cache.user.UserInfoCache;
import com.mtlaa.mtchat.chat.service.ChatService;
import com.mtlaa.mtchat.chat.service.impl.PushService;
import com.mtlaa.mtchat.domain.chat.entity.GroupMember;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.ChatMessageReq;
import com.mtlaa.mtchat.domain.chat.vo.response.wsMsg.WSBaseResp;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.websocket.enums.WebSocketResponseTypeEnum;
import com.mtlaa.mtchat.domain.websocket.vo.WSMemberChange;
import com.mtlaa.mtchat.event.GroupMemberAddEvent;
import com.mtlaa.mtchat.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupMemberAddListener {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private PushService pushService;
    @Autowired
    private GroupMemberCache groupMemberCache;

    /**
     * 在群聊里发送一条消息，消息推送时（非热点群聊）会创建或刷新会话。热点群聊在用户注册时就创建了会话，所以这里可以不用创建会话，直接发送消息
     */
    @Async
    @TransactionalEventListener(value = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent groupMemberAddEvent){
        List<Long> uidList = groupMemberAddEvent.getGroupMembers().stream()
                .map(GroupMember::getUid).collect(Collectors.toList());

        ChatMessageReq chatMessageReq = new ChatMessageReq();
        chatMessageReq.setRoomId(groupMemberAddEvent.getRoomGroup().getRoomId());
        chatMessageReq.setMsgType(MessageTypeEnum.SYSTEM.getType());

        User inviter = userInfoCache.get(groupMemberAddEvent.getInviteUid());
        String sb = "\"" +
                inviter.getName() +
                "\"" +
                "邀请" +
                userInfoCache.getBatch(uidList).values().stream().map(u -> "\"" + u.getName() + "\"").collect(Collectors.joining(",")) +
                "加入群聊";
        chatMessageReq.setBody(sb);
        chatService.sendMsg(User.SYSTEM_UID, chatMessageReq);
    }

    /**
     * 给群聊里所有人推送消息，通知他们有人加入了群聊
     */
    @Async
    @TransactionalEventListener(value = GroupMemberAddEvent.class, fallbackExecution = true)
    public void pushAddGroupMsg(GroupMemberAddEvent event){
        List<Long> uidList = event.getGroupMembers().stream().map(GroupMember::getUid).collect(Collectors.toList());
        List<User> users = new ArrayList<>(userInfoCache.getBatch(uidList).values());
        users.forEach(user -> {
            WSMemberChange wsMemberChange = WSMemberChange.builder()
                    .uid(user.getId())
                    .roomId(event.getRoomGroup().getRoomId())
                    .changeType(WSMemberChange.CHANGE_TYPE_ADD)
                    .activeStatus(user.getActiveStatus())
                    .lastOptTime(DateUtil.LocalDateTime2Date(user.getLastOptTime()))
                    .build();
            pushService.sendPushMsg(
                    new WSBaseResp<>(WebSocketResponseTypeEnum.MEMBER_CHANGE.getType(), wsMemberChange),
                    groupMemberCache.getMemberUidList(event.getRoomGroup().getRoomId()));
        });
    }
}

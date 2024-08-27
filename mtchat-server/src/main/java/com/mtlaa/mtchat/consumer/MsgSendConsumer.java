package com.mtlaa.mtchat.consumer;


import com.mtlaa.mtchat.cache.chat.*;
import com.mtlaa.mtchat.chat.dao.ContactDao;
import com.mtlaa.mtchat.chat.dao.MessageDao;
import com.mtlaa.mtchat.chat.dao.RoomDao;
import com.mtlaa.mtchat.chat.dao.RoomFriendDao;
import com.mtlaa.mtchat.chat.service.ChatService;
import com.mtlaa.mtchat.chat.service.impl.PushService;
import com.mtlaa.mtchat.constant.MQConstant;
import com.mtlaa.mtchat.domain.chat.dto.MsgSendMessageDTO;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.Room;
import com.mtlaa.mtchat.domain.chat.entity.RoomFriend;
import com.mtlaa.mtchat.domain.chat.enums.RoomTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMessageResp;
import com.mtlaa.mtchat.websocket.service.adapter.WebSocketAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create 2023/12/27 17:44
 * 发送消息到MQ后，该消费者可以顺序消费消息
 */
@Component
@RocketMQMessageListener(consumerGroup = MQConstant.SEND_MSG_GROUP, topic = MQConstant.SEND_MSG_TOPIC)
@Slf4j
public class MsgSendConsumer implements RocketMQListener<MsgSendMessageDTO> {
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private ChatService chatService;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private HotRoomCache hotRoomCache;
    @Autowired
    private PushService pushService;
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private RoomFriendCache roomFriendCache;
    
    /**
     * 当收到消息时，推送消息
     * 再次推送到消息队列
     * @param msgSendMessageDTO 收到的消息id
     */
    @Override
    public void onMessage(MsgSendMessageDTO msgSendMessageDTO) {
        Long msgId = msgSendMessageDTO.getMsgId();
        Message message = msgCache.getMsg(msgId);
        Room room = roomCache.get(message.getRoomId());
        log.info("消费者，来自消息队列：{}", message);
        ChatMessageResp messageResp = chatService.getMsgResponse(message, null);  // 获得消息的返回体

        // 推送消息
        if(room.isHotRoom()){
            // 更新对应房间的最新消息id和时间  只有热点群聊更新 room 表的信息（不需要更新所有群成员的contact表记录，避免写扩散）
            roomDao.refreshActiveTime(room.getId(), msgId, message.getCreateTime());
            // 该房间的信息已经更新，删除缓存
            roomCache.delete(room.getId());

            // 是全员群，推送消息到所有用户
            hotRoomCache.refreshActiveTime(room.getId(), message.getCreateTime());
            // 推送给所有人
            pushService.sendPushMsg(WebSocketAdapter.buildMsgSend(messageResp));
        }else{
            // 获得要推送的uid
            List<Long> memberUidList = new ArrayList<>();
            if(room.getType().equals(RoomTypeEnum.GROUP.getType())){
                // 是 普通群聊
                memberUidList = groupMemberCache.getMemberUidList(room.getId());
            }else if(room.getType().equals(RoomTypeEnum.FRIEND.getType())){
                // 是单聊
                RoomFriend roomFriend = roomFriendCache.get(room.getId());
                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2());
            }
            // 更新所有成员的会话时间（这里只有普通群聊和单聊需要更新，全员群为了避免写扩散，只更新room上的）  如果是第一次，则创建会话记录
            contactDao.refreshOrCreateContact(room.getId(), memberUidList, msgId, message.getCreateTime());
            // 推送消息
            pushService.sendPushMsg(WebSocketAdapter.buildMsgSend(messageResp), memberUidList);
        }
    }
}

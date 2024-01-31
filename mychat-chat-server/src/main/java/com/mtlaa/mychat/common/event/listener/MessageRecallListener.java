package com.mtlaa.mychat.common.event.listener;

import com.mtlaa.mychat.chat.dao.RoomFriendDao;
import com.mtlaa.mychat.chat.domain.entity.Room;
import com.mtlaa.mychat.chat.domain.entity.RoomFriend;
import com.mtlaa.mychat.chat.domain.enums.RoomTypeEnum;
import com.mtlaa.mychat.chat.service.cache.GroupMemberCache;
import com.mtlaa.mychat.chat.service.cache.RoomCache;
import com.mtlaa.mychat.chat.service.impl.PushService;
import com.mtlaa.mychat.common.event.MessageRecallEvent;
import com.mtlaa.mychat.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create 2023/12/30 10:08
 */
@Component
public class MessageRecallListener {

    @Autowired
    private PushService pushService;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private GroupMemberCache groupMemberCache;

    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
    public void evictMsgCache(MessageRecallEvent messageRecallEvent){

    }
    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
    public void sendToAll(MessageRecallEvent messageRecallEvent){
        Long roomId = messageRecallEvent.getRecallDTO().getRoomId();
        Room room = roomCache.get(roomId);
        // 撤回消息无需更新相关会话的活跃时间
        // 推送消息
        if(room.isHotRoom()){
            // 是全员群，推送消息到所有用户
            pushService.sendPushMsg(WebSocketAdapter.buildMsgRecall(messageRecallEvent.getRecallDTO()));
        }else{
            // 获得要推送的uid
            List<Long> memberUidList = new ArrayList<>();
            if(room.getType().equals(RoomTypeEnum.GROUP.getType())){
                // 是 普通群聊
                memberUidList = groupMemberCache.getMemberUidList(room.getId());
            }else if(room.getType().equals(RoomTypeEnum.FRIEND.getType())){
                // 是单聊
                RoomFriend roomFriend = roomFriendDao.getByRoomId(room.getId());
                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2());
            }
            // 推送消息
            pushService.sendPushMsg(WebSocketAdapter.buildMsgRecall(messageRecallEvent.getRecallDTO()), memberUidList);
        }
    }
}

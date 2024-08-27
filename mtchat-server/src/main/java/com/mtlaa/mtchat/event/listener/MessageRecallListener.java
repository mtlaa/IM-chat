package com.mtlaa.mtchat.event.listener;


import com.mtlaa.mtchat.cache.chat.GroupMemberCache;
import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.cache.chat.RoomCache;
import com.mtlaa.mtchat.cache.chat.RoomFriendCache;
import com.mtlaa.mtchat.chat.dao.RoomFriendDao;
import com.mtlaa.mtchat.chat.service.impl.PushService;
import com.mtlaa.mtchat.domain.chat.entity.Room;
import com.mtlaa.mtchat.domain.chat.entity.RoomFriend;
import com.mtlaa.mtchat.domain.chat.enums.RoomTypeEnum;
import com.mtlaa.mtchat.event.MessageRecallEvent;
import com.mtlaa.mtchat.websocket.service.adapter.WebSocketAdapter;
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
    private RoomFriendCache roomFriendCache;
    @Autowired
    private GroupMemberCache groupMemberCache;

    /**
     * 消息被撤回，清除缓存。
     * 实际上这里不用清除缓存，因为撤回消息就是更新消息（设置状态和撤回人），我们在更新的时候就删除了缓存
     */
//    @Async
//    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
//    public void evictMsgCache(MessageRecallEvent messageRecallEvent){
//        msgCache.evictMsg(messageRecallEvent.getRecallDTO().getMsgId());
//    }


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
                RoomFriend roomFriend = roomFriendCache.get(room.getId());
                memberUidList = Arrays.asList(roomFriend.getUid1(), roomFriend.getUid2());
            }
            // 推送消息
            pushService.sendPushMsg(WebSocketAdapter.buildMsgRecall(messageRecallEvent.getRecallDTO()), memberUidList);
        }
    }
}

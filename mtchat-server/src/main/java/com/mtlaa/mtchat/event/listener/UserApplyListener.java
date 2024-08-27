package com.mtlaa.mtchat.event.listener;


import com.mtlaa.mtchat.chat.service.impl.PushService;
import com.mtlaa.mtchat.domain.user.entity.UserApply;
import com.mtlaa.mtchat.domain.websocket.vo.WSFriendApply;
import com.mtlaa.mtchat.event.UserApplyEvent;
import com.mtlaa.mtchat.user.dao.UserApplyDao;
import com.mtlaa.mtchat.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Create 2023/12/21 17:36
 */
@Component
public class UserApplyListener {
    @Autowired
    private UserApplyDao userApplyDao;
    @Autowired
    private PushService pushService;

    @EventListener(UserApplyEvent.class)
    @Async
    public void notifyHaveApply(UserApplyEvent userApplyEvent){
        UserApply userApply = userApplyEvent.getUserApply();
        Integer unreadCount = userApplyDao.getUnreadCount(userApply.getTargetId());
        // 推送消息，需要使用到mq
        pushService.sendPushMsg(WebSocketAdapter
                .buildApplySend(new WSFriendApply(userApply.getUid(), unreadCount)), userApply.getTargetId());
    }
}

package com.mtlaa.mychat.common.event.listener;

import com.mtlaa.mychat.common.event.UserBlackEvent;
import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.service.cache.UserCache;
import com.mtlaa.mychat.websocket.domain.enums.WebSocketResponseTypeEnum;
import com.mtlaa.mychat.websocket.domain.vo.WebSocketResponse;
import com.mtlaa.mychat.websocket.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Create 2023/12/22 20:02
 */
@Component
public class UserBlackListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserCache userCache;
    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void sendMsg(UserBlackEvent userBlackEvent){
        User user = userBlackEvent.getUser();
        webSocketService.sendMsgToAll(new WebSocketResponse<>(WebSocketResponseTypeEnum.BLACK.getType(), user.getId()));
    }

    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void updateUserStatus(UserBlackEvent userBlackEvent){
        userDao.invalidUser(userBlackEvent.getUser().getId());
    }

    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void deleteCache(UserBlackEvent userBlackEvent){
        userCache.evictBlackMap();
    }
}

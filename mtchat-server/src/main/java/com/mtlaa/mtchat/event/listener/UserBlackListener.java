package com.mtlaa.mtchat.event.listener;


import com.mtlaa.mtchat.cache.user.UserInfoCache;
import com.mtlaa.mtchat.domain.chat.vo.response.wsMsg.WSBaseResp;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.websocket.enums.WebSocketResponseTypeEnum;
import com.mtlaa.mtchat.event.UserBlackEvent;
import com.mtlaa.mtchat.user.dao.UserDao;
import com.mtlaa.mtchat.cache.user.UserCache;
import com.mtlaa.mtchat.websocket.service.WebSocketService;
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
    @Autowired
    private UserInfoCache userInfoCache;

    /**
     * 拉黑人并不频繁，因此不经过mq，直接推送给所有在线用户
     */
    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void sendMsg(UserBlackEvent userBlackEvent){
        User user = userBlackEvent.getUser();
        webSocketService.sendMsgToAll(new WSBaseResp<>(WebSocketResponseTypeEnum.BLACK.getType(), user.getId()));
    }

    /**
     * 拉黑后，把用户设为不可用
     */
    @Async
    @TransactionalEventListener(classes = UserBlackEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void updateUserStatus(UserBlackEvent userBlackEvent){
        Long uid = userBlackEvent.getUser().getId();
        userDao.invalidUser(uid);
        userInfoCache.delete(uid);
        userCache.refreshUserModifyTime(uid);
    }

//    /**
//     * 当前新增了一个被拉黑的用户，所以删除旧的缓存
//     */
//    @Async
//    @EventListener(classes = UserBlackEvent.class)
//    public void deleteCache(UserBlackEvent userBlackEvent){
//        userCache.evictBlackMap();
//    }
}

package com.mtlaa.mtchat.event.listener;

import com.mtlaa.mtchat.cache.user.UserCache;
import com.mtlaa.mtchat.cache.user.UserInfoCache;
import com.mtlaa.mtchat.chat.service.impl.PushService;
import com.mtlaa.mtchat.domain.chat.vo.response.wsMsg.WSBaseResp;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.enums.UserActiveStatusEnum;
import com.mtlaa.mtchat.domain.websocket.vo.WSOnlineOfflineNotify;
import com.mtlaa.mtchat.event.UserOfflineEvent;
import com.mtlaa.mtchat.user.dao.UserDao;
import com.mtlaa.mtchat.websocket.service.adapter.WebSocketAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserOfflineListener {
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private PushService pushService;


    /**
     * 更新数据库
     */
    @EventListener(UserOfflineEvent.class)
    @Async
    public void updateDB(UserOfflineEvent event){
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setActiveStatus(UserActiveStatusEnum.OFFLINE.getStatus());
        update.setLastOptTime(LocalDateTime.now());
        userDao.updateById(update);
    }
    /**
     * 更新缓存数据，并把用户下线的消息推送给所有在线用户
     */
    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveRedisAndPush(UserOfflineEvent event) {
        User user = event.getUser();
        userCache.offline(user.getId(), user.getLastOptTime());
        //推送给所有在线用户，该用户下线
        pushService.sendPushMsg(WebSocketAdapter.buildOfflineNotify(user));
    }
}

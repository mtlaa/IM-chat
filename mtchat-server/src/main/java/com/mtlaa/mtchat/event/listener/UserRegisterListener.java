package com.mtlaa.mtchat.event.listener;


import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.enums.IdempotentEnum;
import com.mtlaa.mtchat.domain.user.enums.ItemEnum;
import com.mtlaa.mtchat.event.UserRegisterEvent;
import com.mtlaa.mtchat.user.dao.UserDao;
import com.mtlaa.mtchat.user.service.UserBackpackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Create 2023/12/13 16:33
 */
@Component
public class UserRegisterListener {
    @Autowired
    private UserBackpackService userBackpackService;
    @Autowired
    private UserDao userDao;

    /**
     * 异步给新注册用户发送改名卡
     */
    @EventListener(classes = UserRegisterEvent.class)
    @Async
    public void sendModifyNameCard(UserRegisterEvent userRegisterEvent){
        User user = userRegisterEvent.getUser();
        userBackpackService.acquireItem(user.getId(), ItemEnum.MODIFY_NAME_CARD.getId(),
                IdempotentEnum.UID, user.getId().toString());
    }

    /**
     * 异步给新注册用户发放徽章
     */
    @EventListener(classes = UserRegisterEvent.class)
    @Async
    public void sendBadges(UserRegisterEvent userRegisterEvent){
        User user = userRegisterEvent.getUser();
        int count = userDao.count();
        if(count < 10){
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP10_BADGE.getId(),
                    IdempotentEnum.UID, user.getId().toString());
        }
        if(count < 100){
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP100_BADGE.getId(),
                    IdempotentEnum.UID, user.getId().toString());
        }
    }
}

package com.mtlaa.mychat.common.event.listener;

import com.mtlaa.mychat.common.event.UserRegisterEvent;
import com.mtlaa.mychat.user.dao.UserBackpackDao;
import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.domain.enums.IdempotentEnum;
import com.mtlaa.mychat.user.domain.enums.ItemEnum;
import com.mtlaa.mychat.user.service.UserBackpackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Create 2023/12/13 16:33
 */
@Component
public class UserRegisterListener {
    @Autowired
    private UserBackpackService userBackpackService;
    @Autowired
    private UserDao userDao;
    @EventListener(classes = UserRegisterEvent.class)
    @Async
    public void sendModifyNameCard(UserRegisterEvent userRegisterEvent){
        User user = userRegisterEvent.getUser();
        userBackpackService.acquireItem(user.getId(), ItemEnum.MODIFY_NAME_CARD.getId(),
                IdempotentEnum.UID, user.getId().toString());
    }

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

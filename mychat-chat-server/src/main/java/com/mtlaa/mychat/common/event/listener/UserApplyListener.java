package com.mtlaa.mychat.common.event.listener;

import com.mtlaa.mychat.common.event.UserApplyEvent;
import com.mtlaa.mychat.user.dao.UserApplyDao;
import com.mtlaa.mychat.user.domain.entity.UserApply;
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
    @EventListener(UserApplyEvent.class)
    @Async
    public void notifyHaveApply(UserApplyEvent userApplyEvent){
        UserApply userApply = userApplyEvent.getUserApply();
        Integer unreadCount = userApplyDao.getUnreadCount(userApply.getTargetId());
        // TODO 推送消息，需要使用到mq
    }
}

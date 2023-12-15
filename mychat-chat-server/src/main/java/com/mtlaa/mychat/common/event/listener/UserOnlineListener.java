package com.mtlaa.mychat.common.event.listener;

import com.mtlaa.mychat.common.event.UserOnlineEvent;
import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.domain.enums.UserActiveStatusEnum;
import com.mtlaa.mychat.user.service.IpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Create 2023/12/15 17:14
 */
@Component
public class UserOnlineListener {

    @Autowired
    private UserDao userDao;
    @Autowired
    private IpService ipService;
    @EventListener(UserOnlineEvent.class)
    @Async
    public void saveDB(UserOnlineEvent userOnlineEvent){
        User user = userOnlineEvent.getUser();
        User update = User.builder()
                .id(user.getId())
                .updateTime(LocalDateTime.now())
                .lastOptTime(user.getLastOptTime())
                .ipInfo(user.getIpInfo())
                .activeStatus(UserActiveStatusEnum.ONLINE.getStatus())
                .build();
        userDao.updateById(update);
        // 异步解析ip详情（为什么要先入库再解析之后再入库？因为IP解析是比较慢的，还有可能失败，而用户上线比较重要，所以先保存用户上线状态）
        ipService.refreshIpDetailAsync(user.getId());
    }
}

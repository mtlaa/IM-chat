package com.mtlaa.mychat.common.event;

import com.mtlaa.mychat.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Create 2023/12/14 11:09
 * 用户上线的事件
 */
@Getter
public class UserOnlineEvent extends ApplicationEvent {
    private final User user;
    public UserOnlineEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}

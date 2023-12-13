package com.mtlaa.mychat.common.event;

import com.mtlaa.mychat.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * Create 2023/12/13 16:28
 */
@Getter
public class UserRegisterEvent extends ApplicationEvent {
    private final User user;

    public UserRegisterEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}

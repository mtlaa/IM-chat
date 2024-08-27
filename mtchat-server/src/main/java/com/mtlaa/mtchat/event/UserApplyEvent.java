package com.mtlaa.mtchat.event;


import com.mtlaa.mtchat.domain.user.entity.UserApply;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserApplyEvent extends ApplicationEvent {
    private final UserApply userApply;

    public UserApplyEvent(Object source, UserApply userApply) {
        super(source);
        this.userApply = userApply;
    }

}

package com.mtlaa.mtchat.event;

import com.mtlaa.mtchat.domain.chat.entity.GroupMember;
import com.mtlaa.mtchat.domain.chat.entity.RoomGroup;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 添加群成员时，发出消息，创建对应用户的会话
 */
@Getter
public class GroupMemberAddEvent extends ApplicationEvent {
    /**
     * 被邀请的人
     */
    private final List<GroupMember> groupMembers;
    /**
     * 当前群聊
     */
    private final RoomGroup roomGroup;
    /**
     * 邀请人
     */
    private final Long inviteUid;

    public GroupMemberAddEvent(Object source, List<GroupMember> groupMembers, RoomGroup roomGroup, Long inviteUid) {
        super(source);
        this.groupMembers = groupMembers;
        this.roomGroup = roomGroup;
        this.inviteUid = inviteUid;
    }
}

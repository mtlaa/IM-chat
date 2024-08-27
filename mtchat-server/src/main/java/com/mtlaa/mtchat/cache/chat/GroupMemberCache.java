package com.mtlaa.mtchat.cache.chat;


import com.mtlaa.mtchat.chat.dao.GroupMemberDao;
import com.mtlaa.mtchat.chat.dao.RoomGroupDao;
import com.mtlaa.mtchat.domain.chat.entity.RoomGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Create 2023/12/28 17:00
 */
@Component
public class GroupMemberCache {
    @Autowired
    private RoomGroupDao roomGroupDao;
    @Autowired
    private GroupMemberDao groupMemberDao;


    @Cacheable(cacheNames = "member", key = "'roomId:' + #roomId")
    public List<Long> getMemberUidList(Long roomId) {
        RoomGroup roomGroup = roomGroupDao.getByRoomId(roomId);
        if(roomGroup == null){
            return new ArrayList<>();
        }
        return groupMemberDao.getMemberByGroupId(roomGroup.getId());
    }

    @CacheEvict(cacheNames = "member", key = "'roomId:' + #roomId")
    public List<Long> evictMemberUidList(Long roomId) {
        return null;
    }

}

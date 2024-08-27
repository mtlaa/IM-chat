package com.mtlaa.mtchat.cache.chat;


import com.mtlaa.mtchat.cache.AbstractRedisStringCache;
import com.mtlaa.mtchat.chat.dao.RoomGroupDao;
import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.domain.chat.entity.RoomGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create 2023/12/25 14:59
 */
@Component
public class RoomGroupCache extends AbstractRedisStringCache<Long, RoomGroup> {
    @Autowired
    private RoomGroupDao roomGroupDao;
    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.GROUP_INFO_STRING, roomId);
    }

    /**
     * 传进来的是roomId
     * @param roomIds roomIds
     * @return map
     */
    @Override
    protected Map<Long, RoomGroup> load(List<Long> roomIds) {
        List<RoomGroup> roomGroups = roomGroupDao.listByRoomIds(roomIds);
        return roomGroups.stream().collect(Collectors.toMap(RoomGroup::getRoomId, Function.identity()));
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    public void remove(Long roomId) {
        roomGroupDao.removeByRoomId(roomId);
        delete(roomId);
    }
}

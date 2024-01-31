package com.mtlaa.mychat.chat.service.cache;

import com.mtlaa.mychat.chat.dao.RoomDao;
import com.mtlaa.mychat.chat.domain.entity.Room;
import com.mtlaa.mychat.common.constant.RedisKey;
import com.mtlaa.mychat.common.service.cache.AbstractRedisStringCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create 2023/12/25 14:43
 */
@Component
public class RoomCache extends AbstractRedisStringCache<Long, Room> {
    @Autowired
    private RoomDao roomDao;
    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.ROOM_INFO_STRING, roomId);
    }

    @Override
    protected Map<Long, Room> load(List<Long> roomIds) {
        List<Room> rooms = roomDao.listByIds(roomIds);
        return rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
    }

    @Override
    protected Long getExpireSeconds() {
        return 10 * 60L;
    }
}

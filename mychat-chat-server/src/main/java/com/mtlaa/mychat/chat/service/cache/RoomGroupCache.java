package com.mtlaa.mychat.chat.service.cache;

import com.mtlaa.mychat.chat.dao.RoomGroupDao;
import com.mtlaa.mychat.chat.domain.entity.RoomGroup;
import com.mtlaa.mychat.common.constant.RedisKey;
import com.mtlaa.mychat.common.service.cache.AbstractRedisStringCache;
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

    @Override
    protected Map<Long, RoomGroup> load(List<Long> ids) {
        List<RoomGroup> roomGroups = roomGroupDao.listByIds(ids);
        return roomGroups.stream().collect(Collectors.toMap(RoomGroup::getRoomId, Function.identity()));
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }
}

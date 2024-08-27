package com.mtlaa.mtchat.cache.chat;


import com.mtlaa.mtchat.cache.AbstractRedisStringCache;
import com.mtlaa.mtchat.chat.dao.RoomFriendDao;
import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.domain.chat.entity.RoomFriend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create 2024/1/10 12:04
 */
@Component
public class RoomFriendCache extends AbstractRedisStringCache<Long, RoomFriend> {
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.FRIEND_INFO_STRING, roomId);
    }

    @Override
    protected Map<Long, RoomFriend> load(List<Long> req) {
        List<RoomFriend> roomFriends = roomFriendDao.listByRoomIds(req);
        return roomFriends.stream().collect(Collectors.toMap(RoomFriend::getRoomId, Function.identity()));
    }

    @Override
    protected Long getExpireSeconds() {
        return 3 * 60L;
    }
}

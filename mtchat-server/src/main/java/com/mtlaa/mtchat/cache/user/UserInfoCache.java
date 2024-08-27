package com.mtlaa.mtchat.cache.user;


import com.mtlaa.mtchat.cache.AbstractRedisStringCache;
import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.user.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 批量缓存框架，主要用于获取用户信息
 */
@Component
public class UserInfoCache extends AbstractRedisStringCache<Long, User> {
    @Autowired
    private UserDao userDao;

    @Override
    protected String getKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_INFO_STRING, uid);
    }

    @Override
    protected Map<Long, User> load(List<Long> req) {
        List<User> users = userDao.listByIds(req);
        return users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Override
    protected Long getExpireSeconds() {
        return 15 * 60L;
    }
}

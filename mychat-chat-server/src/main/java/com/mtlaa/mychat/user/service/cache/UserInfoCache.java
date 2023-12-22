package com.mtlaa.mychat.user.service.cache;

import com.mtlaa.mychat.common.constant.RedisKey;
import com.mtlaa.mychat.common.service.cache.AbstractRedisStringCache;
import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create 2023/12/22 15:53
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

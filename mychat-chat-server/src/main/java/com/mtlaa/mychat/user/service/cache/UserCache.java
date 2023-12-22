package com.mtlaa.mychat.user.service.cache;

import com.mtlaa.mychat.common.constant.RedisKey;
import com.mtlaa.mychat.user.dao.BlackDao;
import com.mtlaa.mychat.user.dao.UserRoleDao;
import com.mtlaa.mychat.user.domain.entity.Black;
import com.mtlaa.mychat.user.domain.entity.Role;
import com.mtlaa.mychat.user.domain.entity.UserRole;
import com.mtlaa.redis.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Create 2023/12/22 16:57
 */
@Component
public class UserCache {

    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private BlackDao blackDao;

    public List<Long> getUserModifyTime(List<Long> uids) {
        List<String> keys = uids.stream().map(uid -> RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid))
                .collect(Collectors.toList());
        return RedisUtils.mget(keys, Long.class);
    }
    public void refreshUserModifyTime(Long uid){
        String key = RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid);
        RedisUtils.set(key, new Date().getTime());
    }

    @Cacheable(cacheNames = "user", key = "'roleIdByUid:' + #uid")
    public Set<Long> getRoleSet(Long uid) {
        List<UserRole> userRoles = userRoleDao.getByUid(uid);
        return userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
    }

    @Cacheable(cacheNames = "user", key = "'blackList'")
    public Map<Integer, Set<String>> getBlackMap() {
        Map<Integer, List<Black>> collect = blackDao.list().stream().collect(Collectors.groupingBy(Black::getType));
        Map<Integer, Set<String>> result = new HashMap<>(collect.size());
        for (Map.Entry<Integer, List<Black>> entry : collect.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().map(Black::getTarget).collect(Collectors.toSet()));
        }
        return result;
    }
    @CacheEvict(cacheNames = "user", key = "'blackList'")
    public void evictBlackMap() {}
}

package com.mtlaa.mtchat.cache.user;


import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.domain.user.entity.Black;
import com.mtlaa.mtchat.domain.user.entity.UserRole;
import com.mtlaa.mtchat.user.dao.BlackDao;
import com.mtlaa.mtchat.user.dao.UserRoleDao;
import com.mtlaa.mtchat.utils.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User缓存，主要用于缓存黑名单、用户User的更新时间、用户的角色
 */
@Component
public class UserCache {

    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private BlackDao blackDao;
    @Autowired
    private UserInfoCache userInfoCache;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 批量获取指定用户的更新时间缓存
     * @param uids 指定uid列表
     * @return 每个用户的信息更新时间，与uid列表的顺序对应
     */
    public List<Long> getUserModifyTime(List<Long> uids) {
        List<String> keys = uids.stream().map(uid -> RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid))
                .collect(Collectors.toList());
        return RedisUtils.mget(keys, Long.class);
    }

    /**
     * 刷新指定用户在redis中缓存的信息更新时间, 并且清除该用户的信息缓存
     * @param uid uid
     */
    public void refreshUserModifyTime(Long uid){
        userInfoCache.delete(uid);

        String key = RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid);
        RedisUtils.set(key, new Date().getTime());
    }

    @Cacheable(cacheNames = "user", key = "'roleIdByUid:' + #uid")
    public Set<Long> getRoleSet(Long uid) {
        List<UserRole> userRoles = userRoleDao.getByUid(uid);
        return userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
    }
/*
black:
    ip:

    uid:

 */
//    // 黑名单是热点数据，清除后可能会缓存击穿，使用redis的 set结构存储并实时更新-----已实现：BlackCache二级缓存
//    // FIXED 好像不起作用  -- 启动类要加 @EnableCaching 注解
//    // 黑名单数据是需要频繁访问的，本地缓存不设置过期时间。使用注解的Spring cache需要配置文件配置启用
//    @Cacheable(cacheNames = "user", key = "'blackList'")
//    public Map<Integer, Set<String>> getBlackMap() {
//        Map<Integer, List<Black>> collect = blackDao.list().stream().collect(Collectors.groupingBy(Black::getType));
//        Map<Integer, Set<String>> result = new HashMap<>(collect.size());
//        for (Map.Entry<Integer, List<Black>> entry : collect.entrySet()) {
//            result.put(entry.getKey(), entry.getValue().stream().map(Black::getTarget).collect(Collectors.toSet()));
//        }
//        return result;
//    }
//    @CacheEvict(cacheNames = "user", key = "'blackList'")
//    public void evictBlackMap() {}

    public void online(Long id, LocalDateTime lastOptTime) {
        refreshUserModifyTime(id);
        redisTemplate.opsForSet().add(RedisKey.getKey(RedisKey.USER_ONLINE_SET), id);
    }

    public void offline(Long id, LocalDateTime lastOptTime) {
        refreshUserModifyTime(id);
        redisTemplate.opsForSet().remove(RedisKey.getKey(RedisKey.USER_ONLINE_SET), id);
    }

    public Long getOnlineNum(){
        return redisTemplate.opsForSet().size(RedisKey.getKey(RedisKey.USER_ONLINE_SET));
    }
}

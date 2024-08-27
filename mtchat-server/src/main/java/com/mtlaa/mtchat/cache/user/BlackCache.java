package com.mtlaa.mtchat.cache.user;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mtlaa.mtchat.annotation.RedissonLock;
import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.domain.user.entity.Black;
import com.mtlaa.mtchat.domain.user.enums.BlackTypeEnum;
import com.mtlaa.mtchat.user.dao.BlackDao;
import com.mtlaa.mtchat.utils.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 黑名单的二级缓存：
 * <p>因为黑名单数据每个接口都会访问，肯定需要缓存。
 * <p>用Redis缓存的话，每个接口都要访问Redis，响应速度肯定不如本地缓存快；而且黑名单数据较少，我们可以加上本地缓存，构建黑名单数据的二级缓存</p>
 * <p>避免缓存击穿：如果Redis中黑名单数据过期，大量请求会访问到数据库，造成数据库压力大</p>
 * <p>通过更新Mysql黑名单时同步更新Redis，并且Redis缓存不设置过期时间，从而避免Redis的缓存击穿问题</p>
 * <p></p>
 * <p>为了保证缓存一致性，使更新数据库和更新Redis的操作在一个事物内（@Transactional），并且先更新数据库，再更新缓存</p>
 * <p>这样如果更新Redis失败，会触发Mysql的回滚</p>
 */
@Component
public class BlackCache {
    @Autowired
    private BlackDao blackDao;


    /**
     * 一级：本地缓存
     * @return 黑名单缓存
     */
    @Cacheable(cacheNames = "user", key = "'blackList'")
    public Map<Integer, Set<String>> getBlackMap() {
        Set<String> blackUisSet = RedisUtils.sGet(RedisKey.getKey(RedisKey.BLACK_USER_UID_SET));
        Set<String> blackIpSet = RedisUtils.sGet(RedisKey.getKey(RedisKey.BLACK_USER_IP_SET));
        if (CollectionUtil.isEmpty(blackIpSet) || CollectionUtil.isEmpty(blackUisSet)){
            return rebuildRedisBlack();
        }

        Map<Integer, Set<String>> result = new HashMap<>(2);
        result.put(BlackTypeEnum.UID.getType(), blackUisSet);
        result.put(BlackTypeEnum.IP.getType(), blackIpSet);
        return result;
    }

    /**
     * 二级：重建Redis中黑名单缓存。使用分布式锁，避免缓存击穿
     */
    @RedissonLock(key = "rebuildBlack", waitTime = 10, unit = TimeUnit.SECONDS)
    public Map<Integer, Set<String>> rebuildRedisBlack() {
        Set<String> blackUisSet = RedisUtils.sGet(RedisKey.getKey(RedisKey.BLACK_USER_UID_SET));
        Set<String> blackIpSet = RedisUtils.sGet(RedisKey.getKey(RedisKey.BLACK_USER_IP_SET));
        if (CollectionUtil.isNotEmpty(blackIpSet) && CollectionUtil.isNotEmpty(blackUisSet)){
            Map<Integer, Set<String>> result = new HashMap<>(2);
            result.put(BlackTypeEnum.UID.getType(), blackUisSet);
            result.put(BlackTypeEnum.IP.getType(), blackIpSet);
            return result;
        }
        // 查询数据库，更新缓存
        Map<Integer, List<Black>> collect = blackDao.list().stream().collect(Collectors.groupingBy(Black::getType));
        Map<Integer, Set<String>> result = new HashMap<>(collect.size());
        for (Map.Entry<Integer, List<Black>> entry : collect.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().map(Black::getTarget).collect(Collectors.toSet()));

            switch (BlackTypeEnum.of(entry.getKey())){
                case IP:
                    RedisUtils.sSet(RedisKey.getKey(RedisKey.BLACK_USER_IP_SET),
                            entry.getValue().stream().map(Black::getTarget).toArray());
                    break;
                case UID:
                    RedisUtils.sSet(RedisKey.getKey(RedisKey.BLACK_USER_UID_SET),
                            entry.getValue().stream().map(Black::getTarget).toArray());
                    break;
            }
        }
        return result;
    }

    /**
     * 当Redis缓存更新时，清除本地缓存
     */
    @CacheEvict(cacheNames = "user", key = "'blackList'")
    public void evictBlackMap() {}


    /**
     * 更新Redis黑名单，然后删除本地缓存
     * @param id 目标id
     * @param ip1 ip
     * @param ip2 ip
     */
    public void updateRedisBlack(Long id, String ip1, String ip2){
        updateRedis(BlackTypeEnum.UID, id.toString());
        updateRedis(BlackTypeEnum.IP, ip1);
        updateRedis(BlackTypeEnum.IP, ip2);
        evictBlackMap();
    }
    /**
     * 更新Redis黑名单数据
     * @param blackTypeEnum 目标类型
     * @param target 目标
     */
    private void updateRedis(BlackTypeEnum blackTypeEnum, String target){
        if (StrUtil.isBlank(target)){
            return;
        }
        switch (blackTypeEnum){
            case IP:
                RedisUtils.sSet(RedisKey.getKey(RedisKey.BLACK_USER_IP_SET), target);
                break;
            case UID:
                RedisUtils.sSet(RedisKey.getKey(RedisKey.BLACK_USER_UID_SET), target);
                break;
        }
    }

}

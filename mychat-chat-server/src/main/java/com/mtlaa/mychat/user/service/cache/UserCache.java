package com.mtlaa.mychat.user.service.cache;

import com.mtlaa.mychat.common.constant.RedisKey;
import com.mtlaa.redis.utils.RedisUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create 2023/12/22 16:57
 */
@Component
public class UserCache {


    public List<Long> getUserModifyTime(List<Long> uids) {
        List<String> keys = uids.stream().map(uid -> RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid))
                .collect(Collectors.toList());
        return RedisUtils.mget(keys, Long.class);
    }
    public void refreshUserModifyTime(Long uid){
        String key = RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid);
        RedisUtils.set(key, new Date().getTime());
    }
}

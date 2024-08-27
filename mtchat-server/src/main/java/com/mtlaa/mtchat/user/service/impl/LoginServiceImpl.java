package com.mtlaa.mtchat.user.service.impl;

import cn.hutool.core.util.StrUtil;

import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.user.service.LoginService;
import com.mtlaa.mtchat.utils.jwt.JwtProperties;
import com.mtlaa.mtchat.utils.jwt.JwtUtil;
import com.mtlaa.mtchat.utils.redis.RedisUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Create 2023/12/6 17:58
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {
    @Autowired
    private JwtProperties jwtProperties;
    /**
     * 生成jwt，保存在redis中统一管理，用于续期和控制
     */
    @Override
    public String login(Long userId) {
        // 适配多端登录时，查询Redis中已有的jwt，不用重新生成
        String key = RedisKey.getKey(RedisKey.USER_TOKEN_KEY, userId);
        String jwt = RedisUtils.get(key, String.class);
        if (StrUtil.isNotBlank(jwt)){
            return jwt;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        // jwt不设置过期时间
        jwt = JwtUtil.createJWT(jwtProperties.getSecretKey(), claims);

        // redis key = 'mtchat:userToken:uid_{userid}'
        RedisUtils.set(key, jwt, 7, TimeUnit.DAYS);
        return jwt;
    }

    /**
     * 校验 jwt 是否有效：
     *      1、解析 jwt，获得其中保存的 uid
     *      2、根据 uid 生成 redis key，获取保存的 jwt 是否存在、是否和当前 token 相同
     * 有自动续期机制
     * @param token jwt
     * @return uid
     */
    @Override
    public Long getValidUid(String token) {
        if (StrUtil.isBlank(token)){
            return null;
        }
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
        } catch (Exception e){
            return null;
        }

        Long userId = claims.get("id", Long.class);
        if(userId == null) return null;

        String key = RedisKey.getKey(RedisKey.USER_TOKEN_KEY, userId);

        String oldToken = RedisUtils.get(key, String.class);
        if (StrUtil.isBlank(oldToken)) return null;

        if (token.equals(oldToken)){
            // 自动续期
            Long expire = RedisUtils.getExpire(key, TimeUnit.DAYS);
            if(expire < 2){ // 如果有效期小于2天，就续期
                RedisUtils.expire(key, 7, TimeUnit.DAYS);
            }

            return userId;
        } else {
            RedisUtils.del(key);
        }
        return null;
    }
}

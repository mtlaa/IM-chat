package com.mtlaa.mychat.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mtlaa.mychat.common.constant.RedisKey;
import com.mtlaa.mychat.common.properties.JwtProperties;
import com.mtlaa.mychat.common.utils.JwtUtil;
import com.mtlaa.mychat.user.service.LoginService;
import com.mtlaa.redis.utils.RedisUtils;
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
        Map claims = new HashMap();
        claims.put("id", userId);
        String jwt = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

        RedisUtils.set(RedisKey.getKey(userId), jwt, 3, TimeUnit.DAYS);
        return jwt;
    }

    @Override
    public Long getValidUid(String token) {
        Claims claims;
        try {
            claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
        } catch (Exception e){
            return null;
        }

        Long userId = claims.get("id", Long.class);
        if(userId == null) return null;
        String oldToken = RedisUtils.get(RedisKey.getKey(userId), String.class);
        if(StrUtil.isBlank(oldToken)) return null;

        return token.equals(oldToken) ? userId : null;
    }

    @Override
    public void renewalTokenIfNecessary(String token) {
        Long userId = getValidUid(token);
        if(userId == null) return;

        String key = RedisKey.getKey(userId);

        Long expire = RedisUtils.getExpire(key, TimeUnit.DAYS);
        if(expire < 1){ // 如果有效期小于1天，就续期
            RedisUtils.expire(key, 3, TimeUnit.DAYS);
        }
    }
}

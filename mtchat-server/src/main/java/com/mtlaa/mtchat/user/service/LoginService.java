package com.mtlaa.mtchat.user.service;

import org.springframework.stereotype.Service;

/**
 * Create 2023/12/6 17:58
 */
@Service
public interface LoginService {
    String login(Long userId);

    /**
     * 如果token有效，返回uid
     *
     * @param token
     * @return
     */
    Long getValidUid(String token);
}

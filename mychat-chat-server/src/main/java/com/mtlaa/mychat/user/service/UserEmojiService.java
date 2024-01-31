package com.mtlaa.mychat.user.service;

import com.mtlaa.mychat.user.domain.vo.request.UserEmojiReq;
import com.mtlaa.mychat.user.domain.vo.response.UserEmojiResp;

import java.util.List;

/**
 * Create 2024/1/6 20:39
 */
public interface UserEmojiService {
    List<UserEmojiResp> listByUid(Long uid);

    Long insert(Long uid, UserEmojiReq req);

    void delete(Long uid, long id);
}

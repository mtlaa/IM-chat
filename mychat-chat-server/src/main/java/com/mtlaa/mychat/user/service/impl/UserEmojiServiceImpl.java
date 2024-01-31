package com.mtlaa.mychat.user.service.impl;

import com.mtlaa.mychat.common.annotation.RedissonLock;
import com.mtlaa.mychat.common.exception.BusinessException;
import com.mtlaa.mychat.user.dao.UserEmojiDao;
import com.mtlaa.mychat.user.domain.entity.UserEmoji;
import com.mtlaa.mychat.user.domain.vo.request.UserEmojiReq;
import com.mtlaa.mychat.user.domain.vo.response.UserEmojiResp;
import com.mtlaa.mychat.user.service.UserEmojiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Create 2024/1/6 20:39
 */
@Service
public class UserEmojiServiceImpl implements UserEmojiService {
    @Autowired
    private UserEmojiDao userEmojiDao;

    @Override
    public List<UserEmojiResp> listByUid(Long uid) {
        List<UserEmoji> emojis = userEmojiDao.listByUid(uid);
        return emojis.stream().map(emoji -> UserEmojiResp
                .builder()
                .id(emoji.getId())
                .expressionUrl(emoji.getExpressionUrl())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @RedissonLock(key = "#uid")
    public Long insert(Long uid, UserEmojiReq req) {
        // 检查是否超过30个表情包
        int count = userEmojiDao.countByUid(uid);
        if (count >= 30){
            throw new BusinessException("最多添加30个表情");
        }
        UserEmoji userEmoji = userEmojiDao.getByUidAndUrl(uid, req.getExpressionUrl());
        if (Objects.nonNull(userEmoji)){
            throw new BusinessException("已经有该表情包");
        }
        userEmoji = UserEmoji.builder()
                .uid(uid)
                .expressionUrl(req.getExpressionUrl())
                .build();
        userEmojiDao.save(userEmoji);
        return userEmoji.getId();
    }

    @Override
    public void delete(Long uid, long id) {
        UserEmoji userEmoji = userEmojiDao.getById(id);
        if (Objects.isNull(userEmoji)){
            throw new BusinessException("表情不存在，删除失败");
        }
        if (!Objects.equals(uid, userEmoji.getUid())){
            throw new BusinessException("不能删除别人的表情，删除失败");
        }
        userEmojiDao.removeById(id);
    }
}

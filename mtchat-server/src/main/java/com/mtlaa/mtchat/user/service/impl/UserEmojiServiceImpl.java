package com.mtlaa.mtchat.user.service.impl;


import com.mtlaa.mtchat.annotation.RedissonLock;
import com.mtlaa.mtchat.domain.user.entity.UserEmoji;
import com.mtlaa.mtchat.domain.user.vo.request.UserEmojiReq;
import com.mtlaa.mtchat.domain.user.vo.response.UserEmojiResp;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.user.dao.UserEmojiDao;
import com.mtlaa.mtchat.user.service.UserEmojiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    /**
     * 查询当前用户的表情包，返回的是minio的链接
     */
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

    /**
     * 添加表情
     * @param uid 当前用户
     * @param req 表情包图片已经上传到 minio，这里是minio的url，把url保存到数据库
     * @return 该表情在 user_emoji中的 id
     */
    @Override
    @RedissonLock(key = "#uid")  // 分布式锁
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
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userEmojiDao.save(userEmoji);
        return userEmoji.getId();
    }

    /**
     * 删除表情，只删除 user_emoji表中的记录，minio中图片可能被其他用户使用，不删除
     * @param uid 用户id
     * @param id 表情id
     */
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

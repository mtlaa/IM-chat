package com.mtlaa.mychat.user.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtlaa.mychat.common.domain.enums.NormalOrNoEnum;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.domain.entity.UserEmoji;
import com.mtlaa.mychat.user.mapper.UserEmojiMapper;
import com.mtlaa.mychat.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create 2024/1/6 20:45
 */
@Service
public class UserEmojiDao extends ServiceImpl<UserEmojiMapper, UserEmoji> {
    public List<UserEmoji> listByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid)
                .eq(UserEmoji::getDeleteStatus, NormalOrNoEnum.NORMAL.getStatus())
                .list();
    }

    public int countByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid)
                .eq(UserEmoji::getDeleteStatus, NormalOrNoEnum.NORMAL.getStatus())
                .count();
    }

    public UserEmoji getByUidAndUrl(Long uid, String expressionUrl) {
        return lambdaQuery().eq(UserEmoji::getUid, uid)
                .eq(UserEmoji::getExpressionUrl, expressionUrl)
                .one();
    }
}

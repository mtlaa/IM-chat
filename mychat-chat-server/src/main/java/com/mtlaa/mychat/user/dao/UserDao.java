package com.mtlaa.mychat.user.dao;

import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtlaa.mychat.user.service.UserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-11-30
 */
@Service
public class UserDao extends ServiceImpl<UserMapper, User> {

    public User getByOpenId(String openId) {
        return lambdaQuery().eq(User::getOpenId, openId).one();
    }

    public User getByName(String name) {
        return lambdaQuery().eq(User::getName, name).one();
    }
}

package com.mtlaa.mychat.user.dao;

import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtlaa.mychat.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<User> getFriendList(List<Long> friendUids) {
        return lambdaQuery().in(User::getId, friendUids)
                .select(User::getId, User::getName, User::getAvatar, User::getActiveStatus)
                .list();
    }

    public void invalidUser(Long id) {
        lambdaUpdate().set(User::getStatus, 1)
                .eq(User::getId, id)
                .update();
    }

    public List<Long> getAllUid() {
        return lambdaQuery().select(User::getId)
                .list().stream().map(User::getId).collect(Collectors.toList());
    }
}

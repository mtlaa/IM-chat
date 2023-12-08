package com.mtlaa.mychat.user.service.impl;

import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Create 2023/12/6 16:28
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;


    @Override
    @Transactional
    public Long register(User user) {
        userDao.save(user);
        // TODO 用户注册的事件
        return user.getId();
    }
}

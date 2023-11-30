package com.mtlaa.mychat.user.dao;

import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.mapper.UserMapper;
import com.mtlaa.mychat.user.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class UserDao extends ServiceImpl<UserMapper, User> implements IUserService {

}

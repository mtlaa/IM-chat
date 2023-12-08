package com.mtlaa.mychat.user.service;

import com.mtlaa.mychat.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author mtlaa
 * @since 2023-11-30
 */
@Service
public interface UserService {

    Long register(User user);

}

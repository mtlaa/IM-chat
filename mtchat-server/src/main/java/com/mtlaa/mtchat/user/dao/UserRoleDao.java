package com.mtlaa.mtchat.user.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mtlaa.mtchat.domain.user.entity.UserRole;
import com.mtlaa.mtchat.user.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户角色关系表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-22
 */
@Service
public class UserRoleDao extends ServiceImpl<UserRoleMapper, UserRole> {


    public List<UserRole> getByUid(Long uid) {
        return lambdaQuery().eq(UserRole::getUid, uid)
                .list();
    }
}

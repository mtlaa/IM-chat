package com.mtlaa.mychat.user.dao;

import com.mtlaa.mychat.user.domain.entity.UserRole;
import com.mtlaa.mychat.user.mapper.UserRoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

package com.mtlaa.mtchat.user.service.impl;

import com.mtlaa.mtchat.domain.user.enums.RoleEnum;
import com.mtlaa.mtchat.user.service.RoleService;
import com.mtlaa.mtchat.cache.user.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Create 2023/12/22 19:28
 */
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private UserCache userCache;

    @Override
    public boolean hasPower(Long uid, RoleEnum roleEnum) {
        Set<Long> roles = userCache.getRoleSet(uid);
        return roles.contains(roleEnum.getId()) || isAdmin(roles);
    }
    private boolean isAdmin(Set<Long> roles){
        return roles.contains(RoleEnum.ADMIN.getId());
    }
}

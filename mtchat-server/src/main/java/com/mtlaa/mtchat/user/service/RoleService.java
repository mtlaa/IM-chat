package com.mtlaa.mtchat.user.service;


import com.mtlaa.mtchat.domain.user.enums.RoleEnum;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-22
 */
public interface RoleService {
    /**
     * 判断该用户是否有某个权限
     */
    boolean hasPower(Long uid, RoleEnum roleEnum);
}

package com.mtlaa.mychat.user.service;

import com.mtlaa.mychat.user.domain.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mtlaa.mychat.user.domain.enums.RoleEnum;

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

package com.mtlaa.mychat.user.dao;

import com.mtlaa.mychat.user.domain.entity.Role;
import com.mtlaa.mychat.user.mapper.RoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-22
 */
@Service
public class RoleDao extends ServiceImpl<RoleMapper, Role> {

}

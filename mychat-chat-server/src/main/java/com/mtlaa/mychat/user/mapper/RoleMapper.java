package com.mtlaa.mychat.user.mapper;

import com.mtlaa.mychat.user.domain.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 角色表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-22
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

}

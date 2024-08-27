package com.mtlaa.mtchat.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtlaa.mtchat.domain.user.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户角色关系表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-22
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

}

package com.mtlaa.mychat.user.mapper;

import com.mtlaa.mychat.user.domain.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

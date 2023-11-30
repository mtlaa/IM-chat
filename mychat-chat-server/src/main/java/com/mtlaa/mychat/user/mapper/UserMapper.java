package com.mtlaa.mychat.user.mapper;

import com.mtlaa.mychat.user.domain.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-11-30
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}

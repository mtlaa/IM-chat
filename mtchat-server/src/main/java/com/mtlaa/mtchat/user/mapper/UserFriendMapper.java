package com.mtlaa.mtchat.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtlaa.mtchat.domain.user.entity.UserFriend;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户联系人表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Mapper
public interface UserFriendMapper extends BaseMapper<UserFriend> {

}

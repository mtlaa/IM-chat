package com.mtlaa.mychat.user.mapper;

import com.mtlaa.mychat.user.domain.entity.UserFriend;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

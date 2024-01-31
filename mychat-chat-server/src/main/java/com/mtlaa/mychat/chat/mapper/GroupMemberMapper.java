package com.mtlaa.mychat.chat.mapper;

import com.mtlaa.mychat.chat.domain.entity.GroupMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 群成员表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {

}

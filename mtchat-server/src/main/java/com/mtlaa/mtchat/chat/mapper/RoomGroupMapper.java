package com.mtlaa.mtchat.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtlaa.mtchat.domain.chat.entity.RoomGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 群聊房间表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Mapper
public interface RoomGroupMapper extends BaseMapper<RoomGroup> {

}

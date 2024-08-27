package com.mtlaa.mtchat.chat.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mtlaa.mtchat.chat.mapper.RoomGroupMapper;
import com.mtlaa.mtchat.domain.chat.entity.RoomGroup;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 群聊房间表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Service
public class RoomGroupDao extends ServiceImpl<RoomGroupMapper, RoomGroup> {

    public RoomGroup getByRoomId(Long roomId) {
        return lambdaQuery().eq(RoomGroup::getRoomId, roomId).one();
    }

    public void removeByRoomId(Long roomId) {
        LambdaQueryWrapper<RoomGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomGroup::getRoomId, roomId);
        remove(wrapper);
    }

    public List<RoomGroup> listByRoomIds(List<Long> roomIds) {
        return lambdaQuery().in(RoomGroup::getRoomId, roomIds).list();
    }
}

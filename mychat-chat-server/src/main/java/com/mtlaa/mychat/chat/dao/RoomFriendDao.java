package com.mtlaa.mychat.chat.dao;

import com.mtlaa.mychat.common.domain.enums.NormalOrNoEnum;
import com.mtlaa.mychat.chat.domain.entity.RoomFriend;
import com.mtlaa.mychat.chat.mapper.RoomFriendMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 单聊房间表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Service
public class RoomFriendDao extends ServiceImpl<RoomFriendMapper, RoomFriend> {

    public RoomFriend getByRoomKey(String roomKey) {
        return lambdaQuery()
                .eq(RoomFriend::getRoomKey, roomKey)
                .one();
    }

    public void restoreRoom(Long id) {
        lambdaUpdate().set(RoomFriend::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .eq(RoomFriend::getId, id)
                .update();
    }

    public void disableRoom(String roomKey) {
        lambdaUpdate().set(RoomFriend::getStatus, NormalOrNoEnum.NOT_NORMAL.getStatus())
                .eq(RoomFriend::getRoomKey, roomKey)
                .update();
    }

    public RoomFriend getByRoomId(Long roomId) {
        return lambdaQuery().eq(RoomFriend::getRoomId, roomId)
                .one();
    }

    public List<RoomFriend> listByRoomIds(List<Long> req) {
        return lambdaQuery().in(RoomFriend::getRoomId, req).list();
    }
}

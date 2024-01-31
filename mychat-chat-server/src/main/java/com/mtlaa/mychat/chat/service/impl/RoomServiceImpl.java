package com.mtlaa.mychat.chat.service.impl;

import com.mtlaa.mychat.chat.domain.entity.Room;
import com.mtlaa.mychat.chat.domain.enums.HotFlagEnum;
import com.mtlaa.mychat.chat.domain.enums.RoomTypeEnum;
import com.mtlaa.mychat.chat.service.RoomService;
import com.mtlaa.mychat.common.domain.enums.NormalOrNoEnum;
import com.mtlaa.mychat.common.utils.RoomUtils;
import com.mtlaa.mychat.chat.dao.RoomDao;
import com.mtlaa.mychat.chat.dao.RoomFriendDao;
import com.mtlaa.mychat.chat.domain.entity.RoomFriend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Create 2023/12/21 19:50
 */
@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private RoomFriendDao roomFriendDao;


    /**
     * 创建一个单聊
     * 插入room表、room_friend表
     */
    @Override
    public RoomFriend createFriendRoom(Long uid, Long uid1) {
        String roomKey = RoomUtils.generateRoomKey(uid, uid1);
        RoomFriend roomFriend = roomFriendDao.getByRoomKey(roomKey);
        if(Objects.nonNull(roomFriend)){
            restoreRoomIfNeed(roomFriend);
        }else{
            Room room = createRoom(RoomTypeEnum.FRIEND);
            roomFriend = RoomFriend.builder()
                    .roomId(room.getId())
                    .roomKey(roomKey)
                    .uid1(uid)
                    .uid2(uid1)
                    .status(NormalOrNoEnum.NORMAL.getStatus())
                    .build();
            roomFriendDao.save(roomFriend);
        }
        return roomFriend;
    }

    @Override
    public void disableFriendRoom(Long uid, Long targetUid) {
        String roomKey = RoomUtils.generateRoomKey(uid, targetUid);
        roomFriendDao.disableRoom(roomKey);
    }

    @Override
    public RoomFriend getFriendRoom(Long uid, Long uid1) {
        String roomKey = RoomUtils.generateRoomKey(uid, uid1);
        return roomFriendDao.getByRoomKey(roomKey);
    }

    private Room createRoom(RoomTypeEnum roomTypeEnum) {
        Room room = Room.builder()
                .hotFlag(HotFlagEnum.NOT.getType())
                .type(roomTypeEnum.getType())
                .build();
        roomDao.save(room);
        return room;
    }

    private void restoreRoomIfNeed(RoomFriend roomFriend) {
        if(roomFriend.getStatus().equals(NormalOrNoEnum.NOT_NORMAL.getStatus())){
            // 房间被禁用，需要恢复
            roomFriendDao.restoreRoom(roomFriend.getId());
        }
    }


}

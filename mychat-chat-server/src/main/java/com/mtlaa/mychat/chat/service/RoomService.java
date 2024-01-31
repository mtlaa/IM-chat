package com.mtlaa.mychat.chat.service;

import com.mtlaa.mychat.chat.domain.entity.RoomFriend;

/**
 * <p>
 * 房间表 服务类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
public interface RoomService {

    RoomFriend createFriendRoom(Long uid, Long uid1);

    void disableFriendRoom(Long uid, Long targetUid);

    RoomFriend getFriendRoom(Long uid, Long uid1);
}

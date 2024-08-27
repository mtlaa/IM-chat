package com.mtlaa.mtchat.chat.service;


import com.mtlaa.mtchat.domain.chat.entity.RoomFriend;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberAddReq;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberDelReq;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMemberListResp;
import com.mtlaa.mtchat.domain.chat.vo.response.MemberResp;

import java.util.List;

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

    MemberResp getGroupDetail(Long uid, long roomId);

    List<ChatMemberListResp> getMemberList(Long roomId);

    void delMember(Long uid, MemberDelReq request);

    Long addGroup(Long uid, List<Long> uidList);

    void addMember(Long uid, MemberAddReq request);
}

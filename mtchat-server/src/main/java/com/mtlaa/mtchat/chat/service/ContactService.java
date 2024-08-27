package com.mtlaa.mtchat.chat.service;


import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberReq;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMemberResp;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatRoomResp;
import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;

/**
 * <p>
 * 会话列表 服务类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
public interface ContactService {

    CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid);

    ChatRoomResp getContactDetail(Long uid, long roomId);

    ChatRoomResp getContactDetailByFriend(Long uid, Long uid1);

    CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request);
}

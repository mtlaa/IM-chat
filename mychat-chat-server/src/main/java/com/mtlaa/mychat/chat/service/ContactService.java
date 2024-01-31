package com.mtlaa.mychat.chat.service;

import com.mtlaa.mychat.chat.domain.vo.request.member.MemberReq;
import com.mtlaa.mychat.chat.domain.vo.response.ChatMemberResp;
import com.mtlaa.mychat.chat.domain.vo.response.ChatRoomResp;
import com.mtlaa.mychat.common.domain.vo.request.CursorPageBaseReq;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;

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

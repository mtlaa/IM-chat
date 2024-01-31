package com.mtlaa.mychat.chat.service;


import com.mtlaa.mychat.chat.domain.entity.Message;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessageBaseReq;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessageMarkReq;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessagePageReq;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessageReq;
import com.mtlaa.mychat.chat.domain.vo.request.member.MemberReq;
import com.mtlaa.mychat.chat.domain.vo.response.ChatMemberResp;
import com.mtlaa.mychat.chat.domain.vo.response.ChatMessageResp;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;

import java.util.List;

/**
 * Create 2023/12/25 14:18
 */
public interface ChatService {
    Long sendMsg(Long uid, ChatMessageReq chatMessageReq);

    ChatMessageResp getMsgResponse(Message message, Long receivedUid);

    ChatMessageResp getMsgResponse(Long msgId, Long receivedUid);

    CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq pageReq, Long uid);

    void recallMsg(Long uid, ChatMessageBaseReq request);

    void setMsgMark(Long uid, ChatMessageMarkReq request);

    void msgRead(Long uid, Long roomId);
}

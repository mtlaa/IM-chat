package com.mtlaa.mtchat.chat.service;


import com.mtlaa.mtchat.domain.chat.dto.MsgReadInfoDTO;
import com.mtlaa.mtchat.domain.chat.vo.request.*;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMessageReadResp;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMessageResp;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.domain.chat.entity.Message;

import java.util.Collection;

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

    Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq request);

    CursorPageBaseResp<ChatMessageReadResp> getReadPage(Long uid, ChatMessageReadReq request);
}

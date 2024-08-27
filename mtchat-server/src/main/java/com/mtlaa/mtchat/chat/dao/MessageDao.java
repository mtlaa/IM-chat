package com.mtlaa.mtchat.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mtlaa.mtchat.chat.mapper.MessageMapper;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.enums.MessageStatusEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.ChatMessagePageReq;
import com.mtlaa.mtchat.domain.common.enums.NormalOrNoEnum;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.utils.cursor.CursorUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Service
public class MessageDao extends ServiceImpl<MessageMapper, Message> {

    public CursorPageBaseResp<Message> getCursorPage(ChatMessagePageReq pageReq, Long roomId, Long lastMsgId) {
        return CursorUtils.getCursorPageByMysql(this, pageReq, wrapper -> {
            wrapper.eq(Message::getRoomId, roomId);
            wrapper.eq(Message::getStatus, NormalOrNoEnum.NORMAL.getStatus());
            wrapper.le(Objects.nonNull(lastMsgId), Message::getId, lastMsgId);
        }, Message::getId);
    }

    public Integer getGapCount(Long roomId, Long replyMsgId, Long msgId) {
        return lambdaQuery().eq(Message::getRoomId, roomId)
                .gt(Message::getId, replyMsgId)
                .le(Message::getId, msgId)
                .count();
    }

    public Integer unreadCount(LocalDateTime readTime, Long roomId) {
        return lambdaQuery()
                .eq(Message::getRoomId, roomId)
                .gt(Objects.nonNull(readTime), Message::getCreateTime, readTime)
                .count();
    }

    public void removeByRoomIdLogic(Long roomId) {
        lambdaUpdate().eq(Message::getRoomId, roomId)
                .set(Message::getStatus, MessageStatusEnum.DELETE.getStatus())
                .update();
    }
}

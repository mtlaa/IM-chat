package com.mtlaa.mtchat.strategy.msgmark;

import com.mtlaa.mtchat.domain.chat.entity.MessageMark;
import com.mtlaa.mtchat.domain.chat.enums.MessageMarkTypeEnum;
import com.mtlaa.mtchat.domain.common.enums.NormalOrNoEnum;
import org.springframework.stereotype.Component;

@Component
public class MsgDisLikeMark extends AbstractMsgMark{
    @Override
    public MessageMarkTypeEnum getType() {
        return MessageMarkTypeEnum.DISLIKE;
    }

    /**
     * 确认 点踩
     */
    @Override
    protected void doMark(MessageMark messageMark) {
        messageMark.setType(MessageMarkTypeEnum.DISLIKE.getType());
        messageMark.setStatus(NormalOrNoEnum.NORMAL.getStatus());
    }

    /**
     * 取消 点踩
     */
    @Override
    protected void doUnMark(MessageMark messageMark) {
        assertMark(messageMark, MessageMarkTypeEnum.DISLIKE, NormalOrNoEnum.NORMAL);
        messageMark.setStatus(NormalOrNoEnum.NOT_NORMAL.getStatus());
    }
}

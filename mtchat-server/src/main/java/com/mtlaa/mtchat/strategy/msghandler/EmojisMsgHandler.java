package com.mtlaa.mtchat.strategy.msghandler;


import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.msg.EmojisMsgDTO;
import com.mtlaa.mtchat.domain.chat.entity.msg.MessageExtra;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:表情消息
 */
@Component
public class EmojisMsgHandler extends AbstractMsgHandler<EmojisMsgDTO> {
    @Autowired
    private MsgCache msgCache;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.EMOJI;
    }

    @Override
    public void saveMsg(Message msg, EmojisMsgDTO body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setExtra(extra);
        extra.setEmojisMsgDTO(body);
        msgCache.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        return msg.getExtra().getEmojisMsgDTO();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "[图片表情]";
    }

    @Override
    public String showContactMsg(Message msg) {
        return "[图片表情]";
    }
}

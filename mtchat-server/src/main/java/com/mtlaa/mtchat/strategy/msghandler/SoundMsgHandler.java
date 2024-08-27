package com.mtlaa.mtchat.strategy.msghandler;


import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.msg.MessageExtra;
import com.mtlaa.mtchat.domain.chat.entity.msg.SoundMsgDTO;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:语音消息
 */
@Component
public class SoundMsgHandler extends AbstractMsgHandler<SoundMsgDTO> {
    @Autowired
    private MsgCache msgCache;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.SOUND;
    }

    @Override
    public void saveMsg(Message msg, SoundMsgDTO body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setExtra(extra);
        extra.setSoundMsgDTO(body);
        msgCache.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        return msg.getExtra().getSoundMsgDTO();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "[语音消息]";
    }

    @Override
    public String showContactMsg(Message msg) {
        return "[语音消息]";
    }
}

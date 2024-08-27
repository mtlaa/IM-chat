package com.mtlaa.mtchat.strategy.msghandler;


import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.msg.FileMsgDTO;
import com.mtlaa.mtchat.domain.chat.entity.msg.MessageExtra;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 文件消息
 */
@Component
public class FileMsgHandler extends AbstractMsgHandler<FileMsgDTO> {
    @Autowired
    private MsgCache msgCache;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.FILE;
    }

    @Override
    public void saveMsg(Message msg, FileMsgDTO body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setExtra(extra);
        extra.setFileMsg(body);
        msgCache.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        return msg.getExtra().getFileMsg();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "文件:" + msg.getExtra().getFileMsg().getFileName();
    }

    @Override
    public String showContactMsg(Message msg) {
        return "[文件]" + msg.getExtra().getFileMsg().getFileName();
    }
}

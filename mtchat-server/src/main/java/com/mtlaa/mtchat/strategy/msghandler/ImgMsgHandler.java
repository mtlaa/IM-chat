package com.mtlaa.mtchat.strategy.msghandler;


import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.msg.ImgMsgDTO;
import com.mtlaa.mtchat.domain.chat.entity.msg.MessageExtra;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:图片消息
 */
@Component
public class ImgMsgHandler extends AbstractMsgHandler<ImgMsgDTO> {
    @Autowired
    private MsgCache msgCache;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.IMG;
    }

    /**
     * 保存图片消息。在额外信息里面保存图片的文件名、url、大小等信息
     * @param msg 消息的通用信息
     * @param body 消息体以及额外信息
     */
    @Override
    public void saveMsg(Message msg, ImgMsgDTO body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setExtra(extra);
        extra.setImgMsgDTO(body);
        msgCache.updateById(update);
    }

    /**
     * 图片展示
     * @param msg 消息保存体
     */
    @Override
    public Object showMsg(Message msg) {
        return msg.getExtra().getImgMsgDTO();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "[图片]";
    }

    @Override
    public String showContactMsg(Message msg) {
        return "[图片]";
    }
}

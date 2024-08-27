package com.mtlaa.mtchat.strategy.msghandler;



import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Description:系统消息
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-04
 */
@Component
public class SystemMsgHandler extends AbstractMsgHandler<String> {
    @Autowired
    private MsgCache msgCache;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.SYSTEM;
    }

    /**
     * 系统消息，只有消息内容
     * @param msg 消息的通用信息
     * @param body 消息体以及额外信息
     */
    @Override
    public void saveMsg(Message msg, String body) {
        Message update = new Message();
        update.setId(msg.getId());
        update.setContent(body);
        msgCache.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        return msg.getContent();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return msg.getContent();
    }

    @Override
    public String showContactMsg(Message msg) {
        return msg.getContent();
    }
}

package com.mtlaa.mtchat.strategy.msghandler;



import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.cache.user.UserInfoCache;
import com.mtlaa.mtchat.domain.chat.dto.ChatMsgRecallDTO;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.msg.MessageExtra;
import com.mtlaa.mtchat.domain.chat.entity.msg.MsgRecall;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.event.MessageRecallEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * Description: 撤回文本消息
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-04
 */
@Component
public class RecallMsgHandler extends AbstractMsgHandler<Object> {
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.RECALL;
    }

    /**
     * 不支持的操作。撤回消息是调用撤回消息的接口，不会走 checkAndSaveMsg 调用到该方法
     * @param msg 消息的通用信息
     * @param body 消息体以及额外信息
     */
    @Override
    public void saveMsg(Message msg, Object body) {
        throw new UnsupportedOperationException();
    }

    /**
     * 会话内的展示。xxx撤回了一条消息
     * @param msg 消息保存体
     */
    @Override
    public Object showMsg(Message msg) {
        MsgRecall recall = msg.getExtra().getRecall();
        User userInfo = userInfoCache.get(recall.getRecallUid());
        if (!Objects.equals(recall.getRecallUid(), msg.getFromUid())) {
            return "管理员\"" + userInfo.getName() + "\"撤回了一条成员消息";
        }
        return "\"" + userInfo.getName() + "\"撤回了一条消息";
    }

    /**
     * 被回复时的展示
     */
    @Override
    public Object showReplyMsg(Message msg) {
        return "原消息已被撤回";
    }

    /**
     * 执行撤回消息的操作：把消息的状态修改为已撤回，在extra中保存撤回者的信息
     * @param recallUid 撤回人
     * @param message 消息
     */
    public void recall(Long recallUid, Message message) {
        // 把撤回的消息更改为撤回状态，在extra中保存撤回者的信息
        MessageExtra extra = message.getExtra();
        extra.setRecall(new MsgRecall(recallUid, new Date()));
        Message update = new Message();
        update.setId(message.getId());
        update.setType(MessageTypeEnum.RECALL.getType());
        update.setExtra(extra);
        msgCache.updateById(update);
        // 发布撤回消息的事件，需要把该消息的撤回推送给相关用户，让其展示消息已撤回
        applicationEventPublisher.publishEvent(new MessageRecallEvent
                (this, new ChatMsgRecallDTO(message.getId(), message.getRoomId(), recallUid)));
    }

    @Override
    public String showContactMsg(Message msg) {
        return "撤回了一条消息";
    }
}

package com.mtlaa.mychat.chat.service.strategy.msg;


import com.mtlaa.mychat.chat.dao.MessageDao;
import com.mtlaa.mychat.chat.domain.dto.ChatMsgRecallDTO;
import com.mtlaa.mychat.chat.domain.entity.Message;
import com.mtlaa.mychat.chat.domain.entity.msg.MessageExtra;
import com.mtlaa.mychat.chat.domain.entity.msg.MsgRecall;
import com.mtlaa.mychat.chat.domain.enums.MessageTypeEnum;
import com.mtlaa.mychat.common.event.MessageRecallEvent;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.service.cache.UserCache;
import com.mtlaa.mychat.user.service.cache.UserInfoCache;
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
    private MessageDao messageDao;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.RECALL;
    }

    @Override
    public void saveMsg(Message msg, Object body) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object showMsg(Message msg) {
        MsgRecall recall = msg.getExtra().getRecall();
        User userInfo = userInfoCache.get(recall.getRecallUid());
        if (!Objects.equals(recall.getRecallUid(), msg.getFromUid())) {
            return "管理员\"" + userInfo.getName() + "\"撤回了一条成员消息";
        }
        return "\"" + userInfo.getName() + "\"撤回了一条消息";
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "原消息已被撤回";
    }

    public void recall(Long recallUid, Message message) {
        // 把撤回的消息更改为撤回状态，在extra中保存撤回者的信息
        MessageExtra extra = message.getExtra();
        extra.setRecall(new MsgRecall(recallUid, new Date()));
        Message update = new Message();
        update.setId(message.getId());
        update.setType(MessageTypeEnum.RECALL.getType());
        update.setExtra(extra);
        messageDao.updateById(update);
        // 发布撤回消息的事件，需要把该消息的撤回推送给相关用户，让其展示消息已撤回
        applicationEventPublisher.publishEvent(new MessageRecallEvent
                (this, new ChatMsgRecallDTO(message.getId(), message.getRoomId(), recallUid)));
    }

    @Override
    public String showContactMsg(Message msg) {
        return "撤回了一条消息";
    }
}

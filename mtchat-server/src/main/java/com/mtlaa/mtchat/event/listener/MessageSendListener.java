package com.mtlaa.mtchat.event.listener;


import com.mtlaa.mtchat.constant.MQConstant;
import com.mtlaa.mtchat.domain.chat.dto.MsgSendMessageDTO;
import com.mtlaa.mtchat.event.MessageSendEvent;

import com.mtlaa.mychat.transaction.service.MQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Create 2023/12/27 17:12
 */
@Component
public class MessageSendListener {
    @Autowired
    private MQProducer mqProducer;

    /**
     * 把消息发送到 MQ，会触发本地消息表的切面执行
     * 为了保证一致性，该操作需要在事务提交前执行
     * 之后由消费者处理消息的推送
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, classes = MessageSendEvent.class)
    public void messageRoute(MessageSendEvent messageSendEvent){
        Long msgId = messageSendEvent.getMsgId();
        // 为了节省存储和带宽，发送的只是消息id
        mqProducer.sendSecureMsg(MQConstant.SEND_MSG_TOPIC, new MsgSendMessageDTO(msgId), msgId);
    }
}

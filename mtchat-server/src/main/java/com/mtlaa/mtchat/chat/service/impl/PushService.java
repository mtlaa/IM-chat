package com.mtlaa.mtchat.chat.service.impl;


import com.mtlaa.mtchat.constant.MQConstant;
import com.mtlaa.mtchat.domain.chat.dto.PushMessageDTO;
import com.mtlaa.mtchat.domain.chat.vo.response.wsMsg.WSBaseResp;
import com.mtlaa.mychat.transaction.service.MQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// 没有使用事务框架，可能会失败：但是这些消息并不需要保证可靠，
// 因为这些消息最终都是要通过websocket推送给前端，即使没有推送，前端页面刷新一下就都拉取了
/**
 * Description: 推送消息到 MQ
 */
@Service
public class PushService {
    @Autowired
    private MQProducer mqProducer;

    /**
     * 推送消息到 MQ，消息内带有推送的目标uid
     * @param msg 要推送的消息体
     * @param uidList 目标uid列表
     */
    public void sendPushMsg(WSBaseResp<?> msg, List<Long> uidList) {
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC, new PushMessageDTO(uidList, msg));
    }

    /**
     * 推送消息到 MQ，消息内带有推送的目标uid
     * @param msg 要推送的消息体
     * @param uid 目标uid
     */
    public void sendPushMsg(WSBaseResp<?> msg, Long uid) {
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC, new PushMessageDTO(uid, msg));
    }

    /**
     * 推送消息到 MQ，该消息要推送给所有在线用户
     * @param msg 要推送的消息体
     */
    public void sendPushMsg(WSBaseResp<?> msg) {
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC, new PushMessageDTO(msg));
    }
}

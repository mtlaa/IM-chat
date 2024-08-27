package com.mtlaa.mtchat.consumer;


import com.mtlaa.mtchat.constant.MQConstant;
import com.mtlaa.mtchat.domain.chat.dto.PushMessageDTO;
import com.mtlaa.mtchat.domain.websocket.enums.WSPushTypeEnum;
import com.mtlaa.mtchat.websocket.service.WebSocketService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Create 2023/12/28 19:14
 * 从推送队列里消费消息，进行推送
 */
@Component
@RocketMQMessageListener(topic = MQConstant.PUSH_TOPIC, consumerGroup = MQConstant.PUSH_GROUP,
        messageModel = MessageModel.BROADCASTING)   // messageModel为 BROADCASTING广播时，所有的服务都会消费这个消息
                                                    // 为 CLUSTERING集群时，一个消息只会被一个服务消费。因为用户分散在多个webSocket服务上
                                                    // 所以需要使用 BROADCASTING广播模式
public class PushConsumer implements RocketMQListener<PushMessageDTO> {
    @Autowired
    private WebSocketService webSocketService;
    /**
     * 消费消息，使用WebSocket进行推送
     * @param pushMessageDTO 包含消息体和需要推送的uid
     */
    @Override
    public void onMessage(PushMessageDTO pushMessageDTO) {
        WSPushTypeEnum pushTypeEnum = WSPushTypeEnum.of(pushMessageDTO.getPushType());
        switch (pushTypeEnum){
            case USER:
                pushMessageDTO.getUidList().forEach(uid -> {
                    webSocketService.sendMsgToUid(pushMessageDTO.getWsBaseMsg(), uid);
                });
                break;
            case ALL:
                webSocketService.sendMsgToAll(pushMessageDTO.getWsBaseMsg());
                break;
        }
    }
}

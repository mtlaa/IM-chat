package com.mtlaa.mychat.chat.consumer;

import com.mtlaa.mychat.common.constant.MQConstant;
import com.mtlaa.mychat.common.domain.dto.PushMessageDTO;
import com.mtlaa.mychat.websocket.domain.enums.WSPushTypeEnum;
import com.mtlaa.mychat.websocket.domain.vo.WebSocketResponse;
import com.mtlaa.mychat.websocket.service.WebSocketService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.BeanUtils;
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
        WebSocketResponse<?> webSocketResponse = new WebSocketResponse<>();
        BeanUtils.copyProperties(pushMessageDTO.getWsBaseMsg(), webSocketResponse);
        switch (pushTypeEnum){
            case USER:
                pushMessageDTO.getUidList().forEach(uid -> {
                    webSocketService.sendMsgToUid(webSocketResponse, uid);
                });
                break;
            case ALL:
                webSocketService.sendMsgToAll(webSocketResponse);
                break;
        }
    }
}

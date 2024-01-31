package com.mtlaa.mychat;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Create 2023/12/26 15:25
 */
@SpringBootTest
public class MessageTest {
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Test
    public void testSendMQ(){
        Message<String> build = MessageBuilder.withPayload("test rocketMQ").build();
        rocketMQTemplate.send("test-topic", build);
    }
}

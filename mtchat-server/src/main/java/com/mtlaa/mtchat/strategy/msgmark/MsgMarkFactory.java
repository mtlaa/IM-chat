package com.mtlaa.mtchat.strategy.msgmark;

import com.mtlaa.mtchat.domain.chat.enums.MessageMarkTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class MsgMarkFactory {
    private static final Map<MessageMarkTypeEnum, AbstractMsgMark> map = new HashMap<>();

    public static void register(AbstractMsgMark msgMark, MessageMarkTypeEnum markTypeEnum){
        map.put(markTypeEnum, msgMark);
    }

    public static AbstractMsgMark get(MessageMarkTypeEnum typeEnum){
        return map.get(typeEnum);
    }
}

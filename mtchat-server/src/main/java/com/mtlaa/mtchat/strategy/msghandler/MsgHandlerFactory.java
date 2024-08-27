package com.mtlaa.mtchat.strategy.msghandler;



import com.mtlaa.mtchat.exception.BusinessException;

import java.util.HashMap;
import java.util.Map;

/**
 * Create 2023/12/25 15:38
 */
public class MsgHandlerFactory {
    private static final Map<Integer, AbstractMsgHandler<?>> STRATEGY_MAP = new HashMap<>();

    public static void register(Integer type, AbstractMsgHandler<?> handler){
        STRATEGY_MAP.put(type, handler);
    }

    public static AbstractMsgHandler<?> getStrategyNoNull(Integer type){
        AbstractMsgHandler<?> handler = STRATEGY_MAP.getOrDefault(type, null);
        if(handler == null){
            throw new BusinessException("消息类型错误");
        }
        return handler;
    }
}

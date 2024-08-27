package com.mtlaa.mtchat.websocket.utils;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Create 2023/12/8 9:36
 * 把数据（token、ip）保存到 netty 的 Channel 中
 */
public class NettyUtil {
    public static final AttributeKey<String> TOKEN = AttributeKey.valueOf("token");
    public static final AttributeKey<String> IP = AttributeKey.valueOf("ip");
    public static <T> void setAttr(Channel channel, AttributeKey<T> key, T value){
        Attribute<T> attribute = channel.attr(key);
        attribute.set(value);
    }
    public static <T> T getAttr(Channel channel, AttributeKey<T> key){
        return channel.attr(key).get();
    }
}

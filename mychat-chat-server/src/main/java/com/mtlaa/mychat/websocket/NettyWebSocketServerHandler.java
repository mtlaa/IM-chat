package com.mtlaa.mychat.websocket;

import cn.hutool.json.JSONUtil;
import com.mtlaa.mychat.websocket.domain.dto.WebSocketRequest;
import com.mtlaa.mychat.websocket.domain.enums.WebSocketRequestTypeEnum;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Create 2023/11/30 11:10
 * <p>
 * NettyWebSocketServerConfiguration配置类会启动netty服务器，并且添加建立websocket连接需要的处理器
 * 最后还会添加这个类（自定义的处理器，用于接收websocket的消息并处理）为最后一个处理器
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    /**
     * 建立websocket连接(握手)会发出事件：成功或失败
     * 该方法获取到发出的事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            log.info("websocket握手成功");
        }else if(evt instanceof IdleStateEvent){
            // 捕获心跳包发送的空闲事件
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                // 如果是读空闲则关闭ws连接
                ctx.channel().close();
                log.info("读空闲：关闭ws连接...");
                // TODO 用户下线的操作
            }
        }
    }

    /**
     *  当收到一个来自websocket的新消息，该消息被websocket的处理器转换为TextWebSocketFrame格式
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String json = textWebSocketFrame.text();  // 该json是请求体
        WebSocketRequest request = JSONUtil.toBean(json, WebSocketRequest.class);
        log.info("websocket请求：{}", request);
        // 根据请求type进行不同的处理
        // 获取到的websocket消息是TextWebSocketFrame，同样发送给前端的消息也需要是TextWebSocketFrame类型
        switch (WebSocketRequestTypeEnum.of(request.getType())){
            case AUTHORIZE:
                break;
            case HEARTBEAT:
                break;
            case LOGIN:
                log.info("请求登录二维码");
                channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("from web"));
        }
    }
}

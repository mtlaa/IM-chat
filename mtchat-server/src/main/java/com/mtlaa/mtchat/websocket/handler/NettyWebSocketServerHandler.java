package com.mtlaa.mtchat.websocket.handler;


import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.mtlaa.mtchat.domain.websocket.dto.WebSocketRequest;
import com.mtlaa.mtchat.domain.websocket.enums.WebSocketRequestTypeEnum;
import com.mtlaa.mtchat.websocket.service.WebSocketService;
import com.mtlaa.mtchat.websocket.utils.NettyUtil;
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
 *     业务处理器：
 * NettyWebSocketConfiguration配置类会启动netty服务器，并且添加建立websocket连接需要的处理器
 * 最后还会添加这个类（自定义的处理器，用于接收websocket的消息并处理）为最后一个处理器
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketService webSocketService;

    /**
     * 当连接建立时自动调用
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 获取WebSocketService的bean，并且保存，供该处理类使用
        webSocketService = SpringUtil.getBean(WebSocketService.class);

        // 保存当前ws连接的 Channel
        webSocketService.connect(ctx.channel());
    }

    /**
     * 当连接断开时自动调用
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("断开 WebSocket 连接...");
        webSocketService.disconnect(ctx.channel());
    }

    /**
     * 建立websocket连接(握手)会发出事件：成功或失败
     * 该方法获取到发出的事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            String token = NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            webSocketService.handleAuthorizeJwt(ctx.channel(), token);
        }else if(evt instanceof IdleStateEvent){
            // 捕获心跳包发送的空闲事件
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.READER_IDLE){
                log.info("读空闲：关闭ws连接...");
                // 如果是读空闲则关闭ws连接
                ctx.channel().close();   // 会调用到 channelInactive
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("异常发生： ", cause);
        ctx.channel().close();
    }

    /**
     *  当收到一个来自websocket的新消息，该消息被websocket的处理器转换为TextWebSocketFrame格式
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String json = textWebSocketFrame.text();  // 该json是请求体，该请求体由前端发送 （WebSocketRequest的json格式）
        WebSocketRequest request = JSONUtil.toBean(json, WebSocketRequest.class);

        // 根据请求type进行不同的处理
        // 获取到的websocket消息是TextWebSocketFrame，同样发送给前端的消息也需要是TextWebSocketFrame类型
        switch (WebSocketRequestTypeEnum.of(request.getType())){  // 根据类型code获取对应的枚举类型
            case AUTHORIZE:  // 在登录成功后，如果连接断开重新连接，只需要校验jwt
                webSocketService.handleAuthorizeJwt(channelHandlerContext.channel(), request.getData());
                break;
            case HEARTBEAT:
                break;
            case LOGIN:
                webSocketService.handleLoginRequest(channelHandlerContext.channel());
        }
    }
}

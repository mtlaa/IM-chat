package com.mtlaa.mychat.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import com.mtlaa.mychat.websocket.utils.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * Create 2023/12/8 9:24
 * 获取websocket协议升级前的http请求，取出token参数，并删除url中的参数
 */
public class WebSocketAuthorizeHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FullHttpRequest){
            FullHttpRequest request = (FullHttpRequest) msg;
            UrlBuilder url = UrlBuilder.ofHttp(request.uri()); // 需要使用ofHttp，不能使用of
            // jwt
            CharSequence token = url.getQuery().get("token");
            String tokenStr = null;
            if(token != null){
                tokenStr = token.toString();
            }
            // 绑定到该ws的channel中
            NettyUtil.setAttr(ctx.channel(), NettyUtil.TOKEN, tokenStr);
            // 删除url中的参数，否则在WebSocketServerProtocolHandler中会失败
            // 在WebSocketServerProtocolHandler中会判断url是不是和创建该处理器时的入参websocketPath相同
            request.setUri(url.getPath().toString());
            ctx.pipeline().remove(this);

        }
        // 把握手消息传递给后续处理器处理
        ctx.fireChannelRead(msg);
    }
}

package com.mtlaa.mychat.websocket;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.util.StrUtil;
import com.mtlaa.mychat.websocket.utils.NettyUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.InetSocketAddress;

/**
 * Create 2023/12/8 9:24
 * 获取websocket协议升级前的http请求，取出token参数，并删除url中的参数
 * 并且获取http header中的IP
 */
public class WebSocketHeaderHandler extends ChannelInboundHandlerAdapter {
    private static final String IP_HEADER = "X-Real-IP";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)   {
        if(msg instanceof FullHttpRequest){
            FullHttpRequest request = (FullHttpRequest) msg;
            UrlBuilder url = UrlBuilder.ofHttp(request.uri()); // 需要使用ofHttp，不能使用of

            // 获取jwt，在url的参数中
            CharSequence token = url.getQuery().get("token");
            String tokenStr = null;
            if(token != null){
                tokenStr = token.toString();
            }
            // TODO 测试环境免登录
            tokenStr = "eyJhbGciOiJIUzI1NiJ9.eyJkYXRlIjoxNzA0ODY1MTU3NDA1LCJpZCI6MTEwMDV9.RQJ6pQGMUSrLt5CIw7pyrtlxKP_3-aTyEP-YaYc43PM";

            // 获取IP，在http的请求头中
            HttpHeaders headers = request.headers();
            String ip = headers.get(IP_HEADER);
            if(StrUtil.isBlank(ip)){
                InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
                ip = address.getAddress().getHostAddress();
            }

            // 绑定到该ws的channel中
            NettyUtil.setAttr(ctx.channel(), NettyUtil.IP, ip);
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

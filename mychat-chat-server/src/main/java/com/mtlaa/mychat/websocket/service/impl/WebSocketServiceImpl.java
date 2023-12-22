package com.mtlaa.mychat.websocket.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mtlaa.mychat.common.event.UserOnlineEvent;
import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.domain.enums.RoleEnum;
import com.mtlaa.mychat.user.service.LoginService;
import com.mtlaa.mychat.user.service.RoleService;
import com.mtlaa.mychat.websocket.domain.dto.WebSocketConnectInfo;
import com.mtlaa.mychat.websocket.domain.enums.WebSocketResponseTypeEnum;
import com.mtlaa.mychat.websocket.domain.vo.WSLoginUrl;
import com.mtlaa.mychat.websocket.domain.vo.WebSocketResponse;
import com.mtlaa.mychat.websocket.service.WebSocketService;
import com.mtlaa.mychat.websocket.service.adapter.WebSocketAdapter;
import com.mtlaa.mychat.websocket.utils.NettyUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create 2023/12/6 10:46
 */
@Service
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {
    /**
     * 保存当前所有的webSocket连接（登录/游客）
     * 多人聊天室，同时建立了多个webSocket连接。由于存在多线程环境，使用ConcurrentHashMap保存
     */
    private static final Map<Channel, WebSocketConnectInfo> ONLINE_WS_MAP = new ConcurrentHashMap<>();

    public static final Duration DURATION = Duration.ofMinutes(10);  // 过期时间10分钟

    /**
     * 保存待登录的 code-Channel 映射
     * 使用caffeine保存，便于自动过期删除
     */
    private static final Cache<Integer, Channel> WAIT_LOGIN_MAP = Caffeine.newBuilder()
            .maximumSize(10000L)
            .expireAfterWrite(DURATION)
            .build();

    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RoleService roleService;

    /**
     * 保存连接的ws channel
     */
    @Override
    public void connect(Channel channel) {
        // 用户还没有登录，只是游客，WebSocketConnectInfo中userId为null。等登录了再进行赋值
        ONLINE_WS_MAP.put(channel, new WebSocketConnectInfo());
        log.info("当前有" + ONLINE_WS_MAP.size() + "个ws连接：" + ONLINE_WS_MAP.toString());
    }

    /**
     * 由心跳处理器检测到读空闲后发起事件，执行下线操作：删除在线连接中的channel
     */
    @Override
    public void disconnect(Channel channel) {
        WebSocketConnectInfo removeUserInfo = ONLINE_WS_MAP.remove(channel);
        // TODO 用户下线的数据表操作  发出事件
    }

    /**
     * 重新扫码登录时的登录成功
     */
    @Override
    public void loginSuccess(Integer code, User user) {
        log.info("登录成功：{}", user);
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if(channel==null) return;
        WAIT_LOGIN_MAP.invalidate(code);
        // 获取jwt
        String token = loginService.login(user.getId());
        // 返回登录成功的消息
        commonLoginSuccess(channel, user, token);
    }

    /**
     * 发送等待授权的消息
     */
    @Override
    public void waitAuthorizeMsg(Integer code) {
        log.info("等待授权...");
        Channel channel = WAIT_LOGIN_MAP.getIfPresent(code);
        if (channel != null) {
            sendMsg(channel,
                    new WebSocketResponse<>(WebSocketResponseTypeEnum.LOGIN_SCAN_SUCCESS.getType(), null));
        }
    }

    /**
     * 校验jwt是否有效
     */
    @Override
    public void handleAuthorizeJwt(Channel channel, String token) {
        Long userId = loginService.getValidUid(token);
        if(userId == null){
            // 返回消息，需要重新登录
            log.info("解析jwt失败");
            sendMsg(channel, WebSocketAdapter.buildInvalidToken());
        }else{
            // 校验通过
            log.info("解析jwt成功");
            User user = userDao.getById(userId);
            // 使用JWT校验的登录成功
            commonLoginSuccess(channel, user, token);
        }
    }

    @Override
    public void sendMsgToAll(WebSocketResponse<?> msg) {
        ONLINE_WS_MAP.forEach((channel, wci) -> {
            // TODO 使用线程池异步推送所有人
            sendMsg(channel, msg);
        });
    }

    private void commonLoginSuccess(Channel channel, User user, String token) {
        // 保存user与channel的对应关系
        WebSocketConnectInfo webSocketConnectInfo = ONLINE_WS_MAP.get(channel);
        webSocketConnectInfo.setUserId(user.getId());

        // 推送消息
        sendMsg(channel, WebSocketAdapter.build(user, token, roleService.hasPower(user.getId(), RoleEnum.CHAT_MANAGER)));
        // 用户上线事件,发送事件  填充user中字段，如IP信息
        user.setLastOptTime(LocalDateTime.now());
        user.refreshIp(NettyUtil.getAttr(channel, NettyUtil.IP));  // 刷新IP信息
        applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
    }

    /**
     * 处理来自websocket的登录请求，需要通过channel返回微信公众号二维码
     */
    @Override
    public void handleLoginRequest(Channel channel) throws WxErrorException {
        // 生成随机码code，代表一个该连接
        Integer code = generateLoginCode(channel);
        log.info("请求登录二维码，code:{} 当前待登录的连接{}个：{}", code, WAIT_LOGIN_MAP.estimatedSize(), WAIT_LOGIN_MAP.asMap());
        // 用该code申请微信的带参二维码
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService()
                .qrCodeCreateTmpTicket(code, (int) DURATION.toSeconds());
        // 返回二维码给前端
        sendMsg(channel, WebSocketAdapter.build(wxMpQrCodeTicket));
    }


    /**
     * 通过ws发送消息给前端
     */
    private void sendMsg(Channel channel, WebSocketResponse<?> msg) {
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(msg)));
    }

    /**
     * 生成登录的code
     * @return code
     */
    private Integer generateLoginCode(Channel channel){
        Integer code;
        // 循环生成且判断code是否已经存在，如果不存在就再map中建立与channel的映射
        do {
            code = RandomUtil.randomInt(Integer.MAX_VALUE);
        } while (Objects.nonNull(WAIT_LOGIN_MAP.asMap().putIfAbsent(code, channel)));
        return code;
    }


}

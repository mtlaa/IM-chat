package com.mtlaa.mtchat.user.service.handler;


import com.mtlaa.mtchat.user.service.WxMsgService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ScanHandler extends AbstractHandler {
    @Autowired
    private WxMsgService wxMsgService;

    /**
     * 扫码后微信推送消息到controller，然后解析事件类型，执行该事件处理器
     * @param wxMpXmlMessage      微信推送消息
     * @param map        上下文，如果handler或interceptor之间有信息要传递，可以用这个
     * @param wxMpService    服务类
     * @param wxSessionManager session管理器
     * @return WxMpXmlOutMessage 返回的消息，会发送给用户
     */
    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMpXmlMessage, Map<String, Object> map,
                                    WxMpService wxMpService, WxSessionManager wxSessionManager) throws WxErrorException {
        log.info("处理扫码事件：用户扫码，openId：{}", wxMpXmlMessage.getFromUser());

        return wxMsgService.scan(wxMpXmlMessage, wxMpService);
    }

}

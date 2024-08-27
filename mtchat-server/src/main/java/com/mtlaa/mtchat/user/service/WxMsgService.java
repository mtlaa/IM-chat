package com.mtlaa.mtchat.user.service;

import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Service;

/**
 * Create 2023/12/6 15:38
 */
@Service
public interface WxMsgService {
    WxMpXmlOutMessage scan(WxMpXmlMessage wxMpXmlMessage, WxMpService wxMpService);

    void authorize(WxOAuth2UserInfo userInfo);
}

package com.mtlaa.mtchat.user.controller;


import com.mtlaa.mtchat.user.service.WxMsgService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Description: 微信api交互接口
 * Create 2023/11/30 19:59
 *
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("wx/portal/public")
@Api(tags = "微信相关接口")
public class WxPortalController {

    @Autowired
    private WxMsgService wxMsgService;

    private final WxMpService wxService;
    private final WxMpMessageRouter messageRouter;

//    @GetMapping("/test")
//    public String getQrcode(Integer sceneId) throws WxErrorException {
//        log.info("请求二维码...");
//        return wxService.getQrcodeService().qrCodeCreateTmpTicket(sceneId, 1000).getUrl();
//    }

    /**
     * 在微信测试号管理界面配置url提交时会发起该请求
     * 该请求通过appId、secret、token进行校验，返回校验码，与微信的匹配则配置成功
     */
    @GetMapping(produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {
        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature,
                timestamp, nonce, echostr);
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }

        if (wxService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }

        return "非法请求";
    }

    /**
     * 获得code，code用于获取access token
     * 获得access token后才可以拉取用户信息
     * @param code 用户点击授权链接后获得的code
     * @return
     */
    @GetMapping("/callback")
    public RedirectView callBack(@RequestParam String code) throws WxErrorException {
        log.info("用户点击授权链接，callback: code {}", code);
        WxOAuth2AccessToken accessToken = wxService.getOAuth2Service().getAccessToken(code);  // 获取access token
        WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, "zh_CN");
        log.info("获得用户信息：{}", userInfo);
        // 保存用户信息
        wxMsgService.authorize(userInfo);
        // 设置重定向链接，用户点击授权后会重定向到该url
        RedirectView redirectView = new RedirectView("https://github.com/mtlaa");
        return redirectView;
    }

    /**
     * 微信通过该请求推送消息给我们，比如扫码或关注后
     * 根据请求体分发成不同的事件后进行处理
     */
    @PostMapping(produces = "application/xml; charset=UTF-8")
    public String post(@RequestBody String requestBody,
                       @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp,
                       @RequestParam("nonce") String nonce,
                       @RequestParam("openid") String openid,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        log.info("\n接收微信请求：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                openid, signature, encType, msgSignature, timestamp, nonce, requestBody);

        if (!wxService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        String out = null;
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = this.route(inMessage);  // 解析分发成对应的事件，执行事件
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toXml();
        } else if ("aes".equalsIgnoreCase(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxService.getWxMpConfigStorage(),
                    timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toEncryptedXml(wxService.getWxMpConfigStorage());
        }

        log.debug("\n组装回复信息：{}", out);
        return out;
    }

    private WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return this.messageRouter.route(message);  // 分发事件，执行事件对应的处理器
        } catch (Exception e) {
            log.error("路由消息时出现异常！", e);
        }

        return null;
    }
}

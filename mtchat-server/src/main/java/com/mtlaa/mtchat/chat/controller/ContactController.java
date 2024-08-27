package com.mtlaa.mtchat.chat.controller;



import com.mtlaa.mtchat.annotation.RateLimiter;
import com.mtlaa.mtchat.chat.service.ContactService;
import com.mtlaa.mtchat.domain.chat.vo.request.ContactFriendReq;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatRoomResp;
import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.request.IdReqVO;
import com.mtlaa.mtchat.domain.common.vo.response.ApiResult;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * 会话列表 前端控制器
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@RestController
@RequestMapping("/capi/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ContactController {
    @Autowired
    private ContactService contactService;

    @GetMapping("/public/contact/page")
    @ApiOperation("会话列表")
    @RateLimiter(prefix = "contactPage", target = RateLimiter.Target.IP, policy = RateLimiter.Policy.TOKEN_BUCKET)
    public ApiResult<CursorPageBaseResp<ChatRoomResp>> getRoomPage(@Valid CursorPageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(contactService.getContactPage(request, uid));
    }

    @GetMapping("/public/contact/detail")
    @ApiOperation("会话详情")
    public ApiResult<ChatRoomResp> getContactDetail(@Valid IdReqVO request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(contactService.getContactDetail(uid, request.getId()));
    }

    @GetMapping("/public/contact/detail/friend")
    @ApiOperation("会话详情(联系人列表发消息用)")
    public ApiResult<ChatRoomResp> getContactDetailByFriend(@Valid ContactFriendReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(contactService.getContactDetailByFriend(uid, request.getUid()));
    }
}
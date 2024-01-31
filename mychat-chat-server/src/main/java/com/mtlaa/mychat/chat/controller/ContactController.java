package com.mtlaa.mychat.chat.controller;


import com.mtlaa.mychat.chat.dao.ContactDao;
import com.mtlaa.mychat.chat.domain.entity.Contact;
import com.mtlaa.mychat.chat.domain.vo.request.ContactFriendReq;
import com.mtlaa.mychat.chat.domain.vo.response.ChatRoomResp;
import com.mtlaa.mychat.chat.service.ChatService;
import com.mtlaa.mychat.chat.service.ContactService;
import com.mtlaa.mychat.common.domain.vo.request.CursorPageBaseReq;
import com.mtlaa.mychat.common.domain.vo.request.IdReqVO;
import com.mtlaa.mychat.common.domain.vo.response.ApiResult;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;
import com.mtlaa.mychat.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

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
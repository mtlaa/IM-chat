package com.mtlaa.mychat.chat.controller;


import com.mtlaa.mychat.chat.domain.vo.request.*;
import com.mtlaa.mychat.chat.domain.vo.response.ChatMessageResp;
import com.mtlaa.mychat.chat.service.ChatService;
import com.mtlaa.mychat.common.domain.vo.response.ApiResult;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;
import com.mtlaa.mychat.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Create 2023/12/25 14:10
 */
@RestController
@RequestMapping("/capi/chat")
@Slf4j
@Api(tags = "消息相关接口")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/msg")
    @ApiOperation("发送消息")
    public ApiResult<ChatMessageResp> sendMsg(@RequestBody @Valid ChatMessageReq chatMessageReq){
        Long msgId = chatService.sendMsg(RequestHolder.get().getUid(), chatMessageReq);
        return ApiResult.success(chatService.getMsgResponse(msgId, RequestHolder.get().getUid()));
    }

    /**
     * 游标翻页获取消息列表
     */
    @GetMapping("/public/msg/page")
    @ApiOperation("获取消息列表")
    public ApiResult<CursorPageBaseResp<ChatMessageResp>> getPage(@Valid ChatMessagePageReq pageReq){
        CursorPageBaseResp<ChatMessageResp> msgPage = chatService.getMsgPage(pageReq, RequestHolder.get().getUid());
        return ApiResult.success(msgPage);
    }

    /**
     * 撤回消息：查询出要撤回的消息 --> 检查是否能够撤回（是否是自己的消息、是否超时） --> 执行撤回 --> 更改消息状态，在extra中保存撤回者信息
     *         --> 发送撤回消息的事件 --> 事件处理器：把撤回消息的消息推送到消息队列MQ，并指定发送的用户 --> 推送消费者消费消息并通过WS推送
     */
    @PutMapping("/msg/recall")
    @ApiOperation("撤回消息")
    public ApiResult<Void> recallMsg(@RequestBody @Valid ChatMessageBaseReq request){
        chatService.recallMsg(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }

    @PutMapping("/msg/mark")
    @ApiOperation("消息标记")
    public ApiResult<Void> setMsgMark(@Valid @RequestBody ChatMessageMarkReq request) {
        chatService.setMsgMark(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }

    /**
     * 上报阅读到的最新时间线。针对某一个会话（前端打开的那个），每5秒自动上报一次
     */
    @PutMapping("/msg/read")
    @ApiOperation("上报最新的阅读时间线")
    public ApiResult<Void> msgRead(@Valid @RequestBody ChatMessageMemberReq req){
        chatService.msgRead(RequestHolder.get().getUid(), req.getRoomId());
        return ApiResult.success();
    }

}

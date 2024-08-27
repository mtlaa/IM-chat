package com.mtlaa.mtchat.user.controller;


import com.mtlaa.mtchat.domain.common.vo.request.IdReqVO;
import com.mtlaa.mtchat.domain.common.vo.response.ApiResult;
import com.mtlaa.mtchat.domain.common.vo.response.IdRespVO;
import com.mtlaa.mtchat.domain.user.vo.request.UserEmojiReq;
import com.mtlaa.mtchat.domain.user.vo.response.UserEmojiResp;
import com.mtlaa.mtchat.user.service.UserEmojiService;
import com.mtlaa.mtchat.utils.RequestHolder;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Create 2024/1/6 20:35
 */
@RestController
@RequestMapping("/capi/user/emoji")
public class UserEmojiController {
    @Autowired
    private UserEmojiService userEmojiService;

    @GetMapping("/list")
    @ApiOperation("表情包列表")
    public ApiResult<List<UserEmojiResp>> list(){
        return ApiResult.success(userEmojiService.listByUid(RequestHolder.get().getUid()));
    }

    @PostMapping()
    @ApiOperation("新增表情")
    public ApiResult<IdRespVO> addEmoji(@Valid @RequestBody UserEmojiReq req){
        return ApiResult.success(IdRespVO.id(userEmojiService.insert(RequestHolder.get().getUid(), req)));
    }

    @DeleteMapping()
    @ApiOperation("删除表情")
    public ApiResult<Void> deleteEmoji(@RequestBody @Valid IdReqVO idReqVO){
        userEmojiService.delete(RequestHolder.get().getUid(), idReqVO.getId());
        return ApiResult.success();
    }
}

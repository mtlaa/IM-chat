package com.mtlaa.mychat.user.controller;

import com.mtlaa.mychat.common.domain.vo.request.IdReqVO;
import com.mtlaa.mychat.common.domain.vo.response.ApiResult;
import com.mtlaa.mychat.common.domain.vo.response.IdRespVO;
import com.mtlaa.mychat.common.utils.RequestHolder;
import com.mtlaa.mychat.user.domain.vo.request.UserEmojiReq;
import com.mtlaa.mychat.user.domain.vo.response.UserEmojiResp;
import com.mtlaa.mychat.user.service.UserEmojiService;
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

package com.mtlaa.mtchat.user.controller;



import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.request.PageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.response.ApiResult;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.domain.common.vo.response.PageBaseResp;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendApplyReq;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendApproveReq;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendCheckReq;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendDeleteReq;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendApplyResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendCheckResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendUnreadResp;
import com.mtlaa.mtchat.user.service.UserFriendService;
import com.mtlaa.mtchat.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 用户联系人表 前端控制器
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@RestController
@RequestMapping("/capi/user/friend")
@Api(tags = "好友相关接口")
public class UserFriendController {
    @Autowired
    private UserFriendService userFriendService;
    @GetMapping("/page")
    @ApiOperation("联系人列表")
    public ApiResult<CursorPageBaseResp<FriendResp>> friendList(@Valid CursorPageBaseReq cursorPageBaseReq){
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(userFriendService.friendList(uid, cursorPageBaseReq));
    }

    @PostMapping("/apply")
    @ApiOperation("加好友")
    public ApiResult<Void> apply(@RequestBody @Valid FriendApplyReq friendApplyReq){
        userFriendService.apply(RequestHolder.get().getUid(), friendApplyReq);
        return ApiResult.success();
    }

    @GetMapping("/apply/page")
    @ApiOperation("好友申请列表")
    public ApiResult<PageBaseResp<FriendApplyResp>> pageList(@Valid PageBaseReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(userFriendService.pageApplyFriend(uid, request));
    }

    @PutMapping("/apply")
    @ApiOperation("同意申请")
    public ApiResult<Void> applyApprove(@Valid @RequestBody FriendApproveReq request) {
        userFriendService.applyApprove(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }

    @GetMapping("/apply/unread")
    @ApiOperation("申请未读数")
    public ApiResult<FriendUnreadResp> unread() {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(userFriendService.unread(uid));
    }

    @DeleteMapping()
    @ApiOperation("删除好友")
    public ApiResult<Void> delete(@Valid @RequestBody FriendDeleteReq request) {
        Long uid = RequestHolder.get().getUid();
        userFriendService.deleteFriend(uid, request.getTargetUid());
        return ApiResult.success();
    }

    @GetMapping("/check")
    @ApiOperation("批量判断是否是自己好友")
    public ApiResult<FriendCheckResp> check(@Valid FriendCheckReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(userFriendService.check(uid, request));
    }
}


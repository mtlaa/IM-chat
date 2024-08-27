package com.mtlaa.mtchat.chat.controller;


import com.mtlaa.mtchat.chat.service.ContactService;
import com.mtlaa.mtchat.chat.service.GroupMemberService;
import com.mtlaa.mtchat.chat.service.RoomService;
import com.mtlaa.mtchat.domain.chat.vo.request.ChatMessageMemberReq;
import com.mtlaa.mtchat.domain.chat.vo.request.GroupAddReq;
import com.mtlaa.mtchat.domain.chat.vo.request.admin.AdminAddReq;
import com.mtlaa.mtchat.domain.chat.vo.request.admin.AdminRevokeReq;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberAddReq;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberDelReq;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberExitReq;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberReq;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMemberListResp;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMemberResp;
import com.mtlaa.mtchat.domain.chat.vo.response.MemberResp;
import com.mtlaa.mtchat.domain.common.vo.request.IdReqVO;
import com.mtlaa.mtchat.domain.common.vo.response.ApiResult;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.domain.common.vo.response.IdRespVO;
import com.mtlaa.mtchat.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Create 2024/1/10 14:42
 */
@RestController
@RequestMapping("/capi/room")
@Api(tags = "聊天室相关接口")
@Slf4j
public class RoomController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private GroupMemberService groupMemberService;


    @GetMapping("/public/group/member/page")
    @ApiOperation("群成员列表")
    public ApiResult<CursorPageBaseResp<ChatMemberResp>> getMemberPage(@Valid MemberReq request) {
        return ApiResult.success(contactService.getMemberPage(request));
    }

    @GetMapping("/public/group")
    @ApiOperation("群组详情")
    public ApiResult<MemberResp> groupDetail(@Valid IdReqVO request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getGroupDetail(uid, request.getId()));
    }


    @GetMapping("/group/member/list")
    @ApiOperation("房间内的所有群成员列表-@专用")
    public ApiResult<List<ChatMemberListResp>> getMemberList(@Valid ChatMessageMemberReq request) {
        return ApiResult.success(roomService.getMemberList(request.getRoomId()));
    }

    @DeleteMapping("/group/member")
    @ApiOperation("移除成员")
    public ApiResult<Void> delMember(@Valid @RequestBody MemberDelReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.delMember(uid, request);
        return ApiResult.success();
    }

    @DeleteMapping("/group/member/exit")
    @ApiOperation("退出群聊")
    public ApiResult<Boolean> exitGroup(@Valid @RequestBody MemberExitReq request) {
        Long uid = RequestHolder.get().getUid();
        groupMemberService.exitGroup(uid, request.getRoomId());
        return ApiResult.success();
    }

    @PostMapping("/group")
    @ApiOperation("新增群组")
    public ApiResult<IdRespVO> addGroup(@Valid @RequestBody GroupAddReq request) {
        Long uid = RequestHolder.get().getUid();
        Long roomId = roomService.addGroup(uid, request.getUidList());
        return ApiResult.success(IdRespVO.id(roomId));
    }

    @PostMapping("/group/member")
    @ApiOperation("邀请好友")
    public ApiResult<Void> addMember(@Valid @RequestBody MemberAddReq request) {
        Long uid = RequestHolder.get().getUid();
        roomService.addMember(uid, request);
        return ApiResult.success();
    }

    @PutMapping("/group/admin")
    @ApiOperation("添加管理员")
    public ApiResult<Boolean> addAdmin(@Valid @RequestBody AdminAddReq request) {
        Long uid = RequestHolder.get().getUid();
        groupMemberService.addAdmin(uid, request);
        return ApiResult.success();
    }

    @DeleteMapping("/group/admin")
    @ApiOperation("撤销管理员")
    public ApiResult<Boolean> revokeAdmin(@Valid @RequestBody AdminRevokeReq request) {
        Long uid = RequestHolder.get().getUid();
        groupMemberService.revokeAdmin(uid, request);
        return ApiResult.success();
    }

}

package com.mtlaa.mtchat.user.controller;



import com.mtlaa.mtchat.domain.common.vo.response.ApiResult;
import com.mtlaa.mtchat.domain.user.dto.ItemInfoDTO;
import com.mtlaa.mtchat.domain.user.dto.SummeryInfoDTO;
import com.mtlaa.mtchat.domain.user.enums.IdempotentEnum;
import com.mtlaa.mtchat.domain.user.enums.RoleEnum;
import com.mtlaa.mtchat.domain.user.vo.request.*;
import com.mtlaa.mtchat.domain.user.vo.response.BadgeResponse;
import com.mtlaa.mtchat.domain.user.vo.response.UserInfoResponse;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.user.service.RoleService;
import com.mtlaa.mtchat.user.service.UserBackpackService;
import com.mtlaa.mtchat.user.service.UserService;
import com.mtlaa.mtchat.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author mtlaa
 * @since 2023-11-30
 */
@RestController
@RequestMapping("/capi/user")
@Slf4j
@Api(tags = "用户相关接口")
public class  UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserBackpackService userBackpackService;

    @GetMapping("/addBadge")
    public ApiResult<?> addBadge(){
        userBackpackService.acquireItem(10003L, 1L, IdempotentEnum.UID, Long.toString(10003));
        return ApiResult.success();
    }

    
    @GetMapping("/public/test")
    public ApiResult<String> test(){
        log.info("测试公开请求....IP:{}", RequestHolder.get().getIp());
        return ApiResult.success(RequestHolder.get().getIp());
    }

    @GetMapping("/userInfo")
    @ApiOperation("获取用户个人信息")
    public ApiResult<UserInfoResponse> getUserInfo(){
        log.info("获取用户信息：{}", RequestHolder.get());
        return ApiResult.success(userService.getUserInfo(RequestHolder.get().getUid()));
    }

    /**
     * 修改用户名，使用注解判断输入的用户名是否合法
     *
     */
    @PutMapping("/name")
    @ApiOperation("修改用户名")
    public ApiResult<String> modifyUserName(@Valid @RequestBody ModifyNameRequest modifyNameRequest){
        userService.modifyName(RequestHolder.get().getUid(), modifyNameRequest);
        return ApiResult.success();
    }

    /**
     * 获取徽章
     */
    @GetMapping("/badges")
    @ApiOperation("获取用户徽章列表")
    public ApiResult<List<BadgeResponse>> getBadges(){
        log.info("获取用户徽章列表");
        return ApiResult.success(userService.getUserBadges(RequestHolder.get().getUid()));
    }

    @PutMapping("/badge")
    @ApiOperation("佩戴徽章")
    public ApiResult<Void> wearBadge(@Valid @RequestBody WearingBadgeRequest wearingBadgeRequest){
        userService.wearBadge(RequestHolder.get().getUid(), wearingBadgeRequest.getBadgeId());
        return ApiResult.success();
    }

    @PostMapping("/public/summary/userInfo/batch")
    @ApiOperation("用户聚合信息-返回的代表需要刷新的")
    public ApiResult<List<SummeryInfoDTO>> getSummeryUserInfo(@Valid @RequestBody SummeryInfoReq req) {
        return ApiResult.success(userService.getSummeryUserInfo(req));
    }

    @PostMapping("/public/badges/batch")
    @ApiOperation("徽章聚合信息-返回的代表需要刷新的")
    public ApiResult<List<ItemInfoDTO>> getItemInfo(@Valid @RequestBody ItemInfoReq req) {
        return ApiResult.success(userService.getItemInfo(req));
    }

    @PutMapping("/black")
    @ApiOperation("拉黑用户")
    public ApiResult<Void> blackUser(@Valid @RequestBody BlackReq blackReq){
        Long uid = RequestHolder.get().getUid();
        // 判断是否有拉黑权限
        if(!roleService.hasPower(uid, RoleEnum.ADMIN)){
            throw new BusinessException("没有拉黑用户的权限，不是admin");
        }
        userService.blackUser(blackReq.getUid());
        return ApiResult.success();
    }

}


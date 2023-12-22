package com.mtlaa.mychat.user.controller;


import com.mtlaa.mychat.common.domain.vo.response.ApiResult;
import com.mtlaa.mychat.common.utils.RequestHolder;
import com.mtlaa.mychat.user.domain.dto.ItemInfoDTO;
import com.mtlaa.mychat.user.domain.dto.SummeryInfoDTO;
import com.mtlaa.mychat.user.domain.vo.request.ItemInfoReq;
import com.mtlaa.mychat.user.domain.vo.request.ModifyNameRequest;
import com.mtlaa.mychat.user.domain.vo.request.SummeryInfoReq;
import com.mtlaa.mychat.user.domain.vo.request.WearingBadgeRequest;
import com.mtlaa.mychat.user.domain.vo.response.BadgeResponse;
import com.mtlaa.mychat.user.domain.vo.response.UserInfoResponse;
import com.mtlaa.mychat.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
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
public class UserController {

    @Autowired
    private UserService userService;
    
    @GetMapping("/public/test")
    private ApiResult<String> test(){
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

}


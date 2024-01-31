package com.mtlaa.mychat.user.controller;

import com.mtlaa.mychat.common.domain.vo.response.ApiResult;
import com.mtlaa.mychat.common.utils.RequestHolder;
import com.mtlaa.mychat.oss.domain.OssReq;
import com.mtlaa.mychat.oss.domain.OssResp;
import com.mtlaa.mychat.user.domain.vo.request.UploadUrlReq;
import com.mtlaa.mychat.user.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Create 2024/1/6 20:04
 */
@RestController
@RequestMapping("/capi/oss")
@Api(tags = "oss相关接口")
public class OssController {
    @Autowired
    private OssService ossService;

    @GetMapping("/upload/url")
    @ApiOperation("获取临时上传文件的链接")
    public ApiResult<OssResp> getUploadUrl(@Valid UploadUrlReq req){
        return ApiResult.success(ossService.getUploadUrl(RequestHolder.get().getUid(), req));
    }
}

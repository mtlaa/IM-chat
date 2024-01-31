package com.mtlaa.mychat.user.service;

import com.mtlaa.mychat.oss.domain.OssResp;
import com.mtlaa.mychat.user.domain.vo.request.UploadUrlReq;

/**
 * Create 2024/1/6 20:09
 */
public interface OssService {
    OssResp getUploadUrl(Long uid, UploadUrlReq req);
}

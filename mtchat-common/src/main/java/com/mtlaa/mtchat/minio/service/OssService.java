package com.mtlaa.mtchat.minio.service;


import com.mtlaa.mtchat.domain.user.vo.request.UploadUrlReq;
import com.mtlaa.mtchat.minio.domain.OssResp;

/**
 * Create 2024/1/6 20:09
 */
public interface OssService {
    OssResp getUploadUrl(Long uid, UploadUrlReq req);
}

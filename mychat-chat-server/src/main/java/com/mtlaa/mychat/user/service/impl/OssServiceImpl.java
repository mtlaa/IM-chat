package com.mtlaa.mychat.user.service.impl;

import com.mtlaa.mychat.common.exception.BusinessException;
import com.mtlaa.mychat.oss.MinIOTemplate;
import com.mtlaa.mychat.oss.domain.OssReq;
import com.mtlaa.mychat.oss.domain.OssResp;
import com.mtlaa.mychat.user.domain.enums.OssSceneEnum;
import com.mtlaa.mychat.user.domain.vo.request.UploadUrlReq;
import com.mtlaa.mychat.user.service.OssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Create 2024/1/6 20:09
 */
@Service
public class OssServiceImpl implements OssService {
    @Autowired
    private MinIOTemplate minIOTemplate;

    @Override
    public OssResp getUploadUrl(Long uid, UploadUrlReq req) {
        OssSceneEnum sceneEnum = OssSceneEnum.of(req.getScene());
        if (Objects.isNull(sceneEnum)){
            throw new BusinessException("场景有误");
        }
        OssReq ossReq = OssReq.builder()
                .fileName(req.getFileName())
                .filePath(sceneEnum.getPath())
                .uid(uid)
                .build();
        return minIOTemplate.getPreSignedObjectUrl(ossReq);
    }
}

package com.mtlaa.mtchat.minio.service.impl;


import com.mtlaa.mtchat.domain.user.enums.OssSceneEnum;
import com.mtlaa.mtchat.domain.user.vo.request.UploadUrlReq;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.minio.MinIOTemplate;
import com.mtlaa.mtchat.minio.service.OssService;
import com.mtlaa.mtchat.minio.domain.OssReq;
import com.mtlaa.mtchat.minio.domain.OssResp;
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

    /**
     * 生成上传文件的url：
     *      场景 - 年月 - uid - uuid（保证唯一） - 文件后缀名
     *      表情包： ~~/emoji/yyyy-mm/uid/uuid.jpg
     *      聊 天： ~~/chat/yyyy-mm/uid/uuid.jpg
     * 可以下载文件的url: oss endpoint/bucket name/emoji/yyyy-mm/uid/uuid.jpg
     * @return 可以上传文件的url 以及 上传成功后可以下载文件的 url
     */
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

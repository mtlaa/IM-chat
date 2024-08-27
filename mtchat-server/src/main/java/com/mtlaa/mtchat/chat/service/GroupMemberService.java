package com.mtlaa.mtchat.chat.service;

import com.mtlaa.mtchat.domain.chat.vo.request.admin.AdminAddReq;
import com.mtlaa.mtchat.domain.chat.vo.request.admin.AdminRevokeReq;

public interface GroupMemberService {
    void exitGroup(Long uid, Long roomId);

    void addAdmin(Long uid, AdminAddReq request);

    void revokeAdmin(Long uid, AdminRevokeReq request);
}

package com.mtlaa.mychat.user.service;

import com.mtlaa.mychat.common.domain.vo.request.CursorPageBaseReq;
import com.mtlaa.mychat.common.domain.vo.request.PageBaseReq;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;
import com.mtlaa.mychat.common.domain.vo.response.PageBaseResp;
import com.mtlaa.mychat.user.domain.entity.UserFriend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mtlaa.mychat.user.domain.vo.request.friend.FriendApplyReq;
import com.mtlaa.mychat.user.domain.vo.request.friend.FriendApproveReq;
import com.mtlaa.mychat.user.domain.vo.request.friend.FriendCheckReq;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendApplyResp;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendCheckResp;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendResp;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendUnreadResp;

/**
 * <p>
 * 用户联系人表 服务类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
public interface UserFriendService {

    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq cursorPageBaseReq);

    void apply(Long uid, FriendApplyReq friendApplyReq);

    PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request);

    void applyApprove(Long uid, FriendApproveReq request);

    FriendUnreadResp unread(Long uid);

    void deleteFriend(Long uid, Long targetUid);

    FriendCheckResp check(Long uid, FriendCheckReq request);
}

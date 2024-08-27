package com.mtlaa.mtchat.user.service;


import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.request.PageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.domain.common.vo.response.PageBaseResp;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendApplyReq;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendApproveReq;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendCheckReq;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendApplyResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendCheckResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendUnreadResp;

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

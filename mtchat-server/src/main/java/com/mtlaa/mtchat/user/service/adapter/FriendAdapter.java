package com.mtlaa.mtchat.user.service.adapter;



import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.entity.UserApply;
import com.mtlaa.mtchat.domain.user.entity.UserFriend;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendApplyResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendResp;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create 2023/12/21 12:00
 */
public class FriendAdapter {
    public static List<FriendResp> buildFriend(List<UserFriend> list, List<User> users){
        Map<Long, User> map = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        return list.stream().map(userFriend -> {
            FriendResp friendResp = new FriendResp();
            friendResp.setUid(userFriend.getFriendUid());
            User user = map.get(userFriend.getFriendUid());
            if(user != null){
                friendResp.setActiveStatus(user.getActiveStatus());
            }
            return friendResp;
        }).collect(Collectors.toList());
    }

    public static List<FriendApplyResp> buildFriendApplyList(List<UserApply> records) {
        return records.stream().map(userApply -> {
            FriendApplyResp friendApplyResp = new FriendApplyResp();
            friendApplyResp.setUid(userApply.getUid());
            friendApplyResp.setType(userApply.getType());
            friendApplyResp.setApplyId(userApply.getId());
            friendApplyResp.setMsg(userApply.getMsg());
            friendApplyResp.setStatus(userApply.getStatus());
            return friendApplyResp;
        }).collect(Collectors.toList());
    }
}

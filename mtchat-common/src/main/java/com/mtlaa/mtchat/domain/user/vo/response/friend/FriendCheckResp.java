package com.mtlaa.mtchat.domain.user.vo.response.friend;

import com.mtlaa.mtchat.domain.user.entity.UserFriend;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Description: 好友校验
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendCheckResp {

    @ApiModelProperty("校验结果")
    private List<FriendCheck> checkedList;

    @Data
    public static class FriendCheck {
        private Long uid;
        private Boolean isFriend;
    }

    public static FriendCheckResp check(List<Long> uids, List<UserFriend> userFriends){
        Set<Long> friendUids = userFriends.stream().map(UserFriend::getFriendUid).collect(Collectors.toSet());
        return new FriendCheckResp(uids.stream().map(uid -> {
            FriendCheck friendCheck = new FriendCheck();
            friendCheck.setUid(uid);
            friendCheck.setIsFriend(friendUids.contains(uid));
            return friendCheck;
        }).collect(Collectors.toList()));
    }

}

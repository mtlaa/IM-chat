package com.mtlaa.mtchat.domain.websocket.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-03-19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSFriendApply {
    /**
     * 申请人
     */
    private Long uid;
    /**
     * 申请未读数
     */
    private Integer unreadCount;
}

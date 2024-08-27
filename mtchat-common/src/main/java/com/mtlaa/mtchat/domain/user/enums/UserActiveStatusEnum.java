package com.mtlaa.mtchat.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create 2023/12/15 17:21
 */
@Getter
@AllArgsConstructor
public enum UserActiveStatusEnum {
    ONLINE(1, "在线"),
    OFFLINE(2, "离线");

    private final Integer status;
    private final String desc;
}

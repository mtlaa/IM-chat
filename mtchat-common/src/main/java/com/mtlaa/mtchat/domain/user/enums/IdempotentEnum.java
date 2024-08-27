package com.mtlaa.mtchat.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create 2023/12/13 9:40
 */
@AllArgsConstructor
@Getter
public enum IdempotentEnum {
    UID(1, "uid"),
    MSG_ID(2, "消息id");


    private final Integer type;
    private final String desc;
}

package com.mtlaa.mtchat.domain.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Create 2023/12/11 17:26
 */
@Data
@AllArgsConstructor
public class RequestInfo {
    private Long uid;
    private String ip;
}

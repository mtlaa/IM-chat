package com.mtlaa.mtchat.domain.websocket.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 被拉黑的用户id
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSBlack {
    private Long uid;
}

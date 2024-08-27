package com.mtlaa.mtchat.domain.websocket.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 推送消息已读人数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSMessageRead {
    /**
     * 消息id
     */
    private Long msgId;
    /**
     * 已阅读人数（可能为0）
     */
    private Integer readCount;
}

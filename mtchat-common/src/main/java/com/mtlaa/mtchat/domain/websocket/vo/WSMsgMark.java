package com.mtlaa.mtchat.domain.websocket.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Description: 消息标记
 * 一个用户标记别人发的消息后，会把标记信息推送给其他人
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WSMsgMark {
    private List<WSMsgMarkItem> markList;

    @Data
    public static class WSMsgMarkItem {
        /**
         * 标记操作者
         */
        private Long uid;
        /**
         * 被标记的消息id
         */
        private Long msgId;
        /**
         * @see com.abin.mallchat.common.chat.domain.enums.MessageMarkTypeEnum
         * 标记类型 1点赞 2举报
         */
        private Integer markType;
        /**
         * 被标记的数量
         */
        private Integer markCount;
        /**
         * @see com.abin.mallchat.common.chat.domain.enums.MessageMarkActTypeEnum
         * 动作类型 1确认 2取消
         */
        private Integer actType;
    }
}

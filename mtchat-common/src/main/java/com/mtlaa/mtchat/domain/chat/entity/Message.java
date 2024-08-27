package com.mtlaa.mtchat.domain.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.mtlaa.mtchat.domain.chat.entity.msg.MessageExtra;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 消息表
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "message", autoResultMap = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会话表id
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 消息发送者uid
     */
    @TableField("from_uid")
    private Long fromUid;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 回复的消息内容
     */
    @TableField("reply_msg_id")
    private Long replyMsgId;

    /**
     * 消息状态 0正常 1删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 与回复的消息间隔多少条
     */
    @TableField("gap_count")
    private Integer gapCount;

    /**
     * 消息类型 1正常文本 2.撤回消息
     */
    @TableField("type")
    private Integer type;

    /**
     * 扩展信息
     */
    @TableField(value = "extra", typeHandler = JacksonTypeHandler.class)
    private MessageExtra extra;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}

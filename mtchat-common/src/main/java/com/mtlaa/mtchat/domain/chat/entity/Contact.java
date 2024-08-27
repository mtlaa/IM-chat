package com.mtlaa.mtchat.domain.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 会话列表
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("contact")
public class Contact implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * uid
     */
    @TableField("uid")
    private Long uid;

    /**
     * 房间id
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * 阅读到的时间
     */
    @TableField("read_time")
    private LocalDateTime readTime;

    /**
     * 会话内消息最后更新的时间(只有普通会话需要维护，全员会话不需要维护)
     */
    @TableField("active_time")
    private LocalDateTime activeTime;

    /**
     * 会话最新消息id
     */
    @TableField("last_msg_id")
    private Long lastMsgId;

    /**
     * 创建时间
     */
      @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
      @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}

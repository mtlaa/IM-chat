package com.mtlaa.mtchat.domain.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 单聊房间表
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("room_friend")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomFriend implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 房间id
     */
    @TableField("room_id")
    private Long roomId;

    /**
     * uid1（更小的uid）
     */
    @TableField("uid1")
    private Long uid1;

    /**
     * uid2（更大的uid）
     */
    @TableField("uid2")
    private Long uid2;

    /**
     * 房间key由两个uid拼接，先做排序uid1_uid2
     */
    @TableField("room_key")
    private String roomKey;

    /**
     * 房间状态 0正常 1禁用(删好友了禁用)
     */
    @TableField("status")
    private Integer status;

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

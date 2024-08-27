package com.mtlaa.mtchat.domain.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 群成员表
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("group_member")
public class GroupMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 群主id
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 成员uid
     */
    @TableField("uid")
    private Long uid;

    /**
     * 成员角色 1群主 2管理员 3普通成员
     */
    @TableField("role")
    private Integer role;

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

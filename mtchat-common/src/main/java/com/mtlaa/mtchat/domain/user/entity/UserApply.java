package com.mtlaa.mtchat.domain.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户申请表
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_apply")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserApply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申请人uid
     */
    @TableField("uid")
    private Long uid;

    /**
     * 申请类型 1加好友
     */
    @TableField("type")
    private Integer type;

    /**
     * 接收人uid
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 申请信息
     */
    @TableField("msg")
    private String msg;

    /**
     * 申请状态 1待审批 2同意
     */
    @TableField("status")
    private Integer status;

    /**
     * 阅读状态 1未读 2已读
     */
    @TableField("read_status")
    private Integer readStatus;

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

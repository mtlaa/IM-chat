package com.mtlaa.mychat.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author mtlaa
 * @since 2023-11-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "user", autoResultMap = true)  // 开启自动映射字段
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    @TableField("name")
    private String name;

    /**
     * 用户头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别 1为男性，2为女性
     */
    @TableField("sex")
    private Integer sex;

    /**
     * 微信openid用户标识
     */
    @TableField("open_id")
    private String openId;

    /**
     * 在线状态 1在线 2离线
     */
    @TableField("active_status")
    private Integer activeStatus;

    /**
     * 最后上下线时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @TableField("last_opt_time")
    private LocalDateTime lastOptTime;

    /**
     * ip信息
     */
    @TableField(value = "ip_info", typeHandler = JacksonTypeHandler.class)  // 自动转换该字段为json
    private IpInfo ipInfo;

    /**
     * 佩戴的徽章id
     */
    @TableField("item_id")
    private Long itemId;

    /**
     * 使用状态 0.正常 1拉黑
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    public void refreshIp(String ip){
        if(ipInfo == null){
            ipInfo = new IpInfo();
        }
        ipInfo.refreshIp(ip);
    }
}

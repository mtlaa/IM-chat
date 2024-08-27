package com.mtlaa.mtchat.domain.chat.entity.msg;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 语音消息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SoundMsgDTO extends BaseFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("时长（秒）")
    @NotNull
    private Integer second;
}

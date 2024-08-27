package com.mtlaa.mtchat.domain.user.vo.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


/**
 * Description: 表情包反参
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-07-09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEmojiReq {
    /**
     * 表情地址
     */
    @ApiModelProperty(value = "新增的表情url")
    @NotNull
    private String expressionUrl;

}

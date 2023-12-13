package com.mtlaa.mychat.user.domain.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * Create 2023/12/11 19:42
 */
@Data
public class ModifyNameRequest {
    @ApiModelProperty("新用户名")
    @NotBlank
    @Length(max = 6, message = "用户名不能超过6个字符")
    private String name;
}

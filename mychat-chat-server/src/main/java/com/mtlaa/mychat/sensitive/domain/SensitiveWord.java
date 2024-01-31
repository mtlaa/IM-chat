package com.mtlaa.mychat.sensitive.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Create 2024/1/7 13:52
 */
@Data
@TableName("sensitive_word")
public class SensitiveWord {
    @TableField("word")
    private String word;
}

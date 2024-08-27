package com.mtlaa.mtchat.sensitive.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create 2024/1/7 13:52
 */
@Data
@TableName("sensitive_word")
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveWord {
    @TableField("word")
    private String word;
}

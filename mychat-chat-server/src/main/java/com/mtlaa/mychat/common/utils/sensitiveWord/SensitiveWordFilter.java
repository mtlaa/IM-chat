package com.mtlaa.mychat.common.utils.sensitiveWord;

import java.util.List;

/**
 * Create 2024/1/7 13:38
 */
public interface SensitiveWordFilter {
    /**
     * 是否存在敏感词
     * @param text 文本
     * @return boolean
     */
    boolean hasSensitiveWord(String text);

    /**
     * 过滤，返回过滤后的文本
     * @param text 文本
     * @return {@link String}
     */
    String filter(String text);

    /**
     * 加载敏感词列表
     * @param words 敏感词数组
     */
    void loadWord(List<String> words);
}

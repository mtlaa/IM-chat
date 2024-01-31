package com.mtlaa.mychat.common.utils.sensitiveWord;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 敏感词
 *
 * @author zhaoyuhang
 * @date 2023/07/09
 */

public interface IWordFactory {
    /**
     * 返回敏感词数据源
     *
     * @return 结果
     * @since 0.0.13
     */
    List<String> getWordList();
}

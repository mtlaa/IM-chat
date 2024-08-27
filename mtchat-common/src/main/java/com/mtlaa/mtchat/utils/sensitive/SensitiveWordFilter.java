package com.mtlaa.mtchat.utils.sensitive;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 敏感词的统一接口，有两种实现：DFA、AC自动机
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
     * 加载敏感词列表，构建过滤器
     * @param words 敏感词数组
     */
    void loadWord(List<String> words);


    char replace = '*'; // 替代字符
    String skipChars = " !*-+_=,，.@;:；：。、？?'（）()【】[]《》<>“”\"‘’"; // 遇到这些字符就会跳过
}

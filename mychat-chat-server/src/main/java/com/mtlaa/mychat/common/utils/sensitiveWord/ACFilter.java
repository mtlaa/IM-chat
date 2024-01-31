package com.mtlaa.mychat.common.utils.sensitiveWord;

import java.util.List;

/**
 * Create 2024/1/7 15:17
 */
public class ACFilter implements SensitiveWordFilter{
    @Override
    public boolean hasSensitiveWord(String text) {
        return false;
    }

    @Override
    public String filter(String text) {
        return null;
    }

    @Override
    public void loadWord(List<String> words) {

    }
}

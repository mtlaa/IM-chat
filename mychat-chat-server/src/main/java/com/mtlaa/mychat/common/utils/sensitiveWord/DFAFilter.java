package com.mtlaa.mychat.common.utils.sensitiveWord;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.tokenizer.Word;

import java.util.*;

/**
 * Create 2024/1/7 13:58
 * 前缀树快速匹配字符串（DFA，有限状态自动机）
 */
public class DFAFilter implements SensitiveWordFilter{
    private static final Word root = new Word(' '); // 敏感词字典的根节点
    private final static char replace = '*'; // 替代字符
    private final static String skipChars = " !*-+_=,，.@;:；：。、？?'（）()【】[]《》<>“”\"‘’"; // 遇到这些字符就会跳过
    private final static Set<Character> skipSet = new HashSet<>(); // 遇到这些字符就会跳过

    static {
        for (char c : skipChars.toCharArray()) {
            skipSet.add(c);
        }
    }

    private static class Word {
        private final char c;
        private boolean end;
        private final Map<Character, Word> next;
        public Word(char c){
            this.c = c;
            next = new HashMap<>();
            end = false;
        }
    }

    public static SensitiveWordFilter getInstance() {
        return new DFAFilter();
    }

    @Override
    public boolean hasSensitiveWord(String text) {
        if (StrUtil.isBlank(text)){
            return false;
        }
        return Objects.equals(filter(text), text);
    }

    @Override
    public String filter(String text) {
        StringBuilder result = new StringBuilder(text);
        int start = 0;
        while (start < result.length()){
            char c = result.charAt(start);
            if (skip(c)) {
                start++;
                continue;
            }
            Word cur = root;
            for (int i = start; i < result.length(); i++){
                c = result.charAt(i);
                if (skip(c)){
                    continue;
                }
                if (c >= 'A' && c <= 'Z') {
                    c += 32;
                }
                Word next = cur.next.get(c);
                if (next == null){
                    break;
                }
                if (next.end){
                    for (int j = start; j <= i; j++){
                        result.setCharAt(j, replace);
                    }
                    start = i;
                }
                cur = next;
            }
            start++;
        }
        return result.toString();
    }

    @Override
    public void loadWord(List<String> words) {
        for (String word : words){
            Word cur = root;
            for (int i = 0; i < word.length(); i++){
                char c = word.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    c += 32;
                }
                if (skip(c)){
                    continue;
                }
                if (cur.next.containsKey(c)){
                    cur = cur.next.get(c);
                }else{
                    Word next = new Word(c);
                    cur.next.put(c, next);
                    cur = next;
                }
            }
            cur.end = true;  // 该词的最后一个
        }
    }

    private boolean skip(char c){
        return skipSet.contains(c);
    }
}

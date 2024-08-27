package com.mtlaa.mtchat.utils.sensitive.impl;

import com.mtlaa.mtchat.utils.sensitive.SensitiveWordFilter;

import java.util.*;

public class DFAFilter implements SensitiveWordFilter {
    private final static Set<Character> skipSet = new HashSet<>(); // 遇到这些字符就会跳过
    static {
        for (char c : skipChars.toCharArray()) {
            skipSet.add(c);
        }
    }

    private final static TrieNode root = new TrieNode();  // 前缀树的根节点

    private static class TrieNode{
        private boolean isEnd;
        private final Map<Character, TrieNode> next;
        public TrieNode(){
            isEnd = false;
            next = new HashMap<>();
        }
    }

    @Override
    public boolean hasSensitiveWord(String text) {
        if (Objects.isNull(text)) return false;
        return filter(text).equals(text);
    }

    /**
     * 把 text 中的敏感词字符替换为 *
     * @param text 文本
     * @return 过滤后的
     */
    @Override
    public String filter(String text) {
        StringBuilder ret = new StringBuilder(text);
        int start = 0;
        while (start < ret.length()) {
            char c = Character.toLowerCase(ret.charAt(start));
            if (skipSet.contains(c)) {
                start++;
                continue;
            }

            TrieNode prev = root;
            for (int i = start; i < text.length(); i++) {
                c = Character.toLowerCase(text.charAt(i));
                if (skipSet.contains(c)) {
                    continue;
                }

                if (prev.next.containsKey(c)) {
                    prev = prev.next.get(c);
                } else {
                    break;  // 匹配失败，从start的下一个字符重新开始
                }
                if (prev.isEnd){
                    for (int j = start; j <= i; j++) {
                        ret.setCharAt(j, replace);
                    }
                    start = i;  // 匹配成功，更新start跳过匹配成功的敏感词，然后从start的下一个字符重新开始
                }
            }
            start++;
        }
        return ret.toString();
    }

    /**
     * 遍历每个敏感词，构造前缀树
     * @param words 敏感词数组
     */
    @Override
    public void loadWord(List<String> words) {
        for (String word : words){
            TrieNode prev = root;
            for (int i = 0; i < word.length(); i++) {
                char c = Character.toLowerCase(word.charAt(i));  // 统一转为小写，避免大小写判断
                if (skipSet.contains(c)){  // 特殊字符跳过
                    continue;
                }

                if (prev.next.containsKey(c)){   // 该字符已经在前缀树中，移动到其节点
                    prev = prev.next.get(c);
                    continue;
                }

                TrieNode cur = new TrieNode();  // 为该字符构建节点
                prev.next.put(c, cur);
                prev = cur;

//                if (i == word.length()-1){  // 如果是最后一个字符，isEnd = true
//                    cur.isEnd = true;
//                }
            }
            if (prev != root){
                prev.isEnd = true;
            }
        }
    }
}

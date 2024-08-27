package com.mtlaa.mtchat.utils.sensitive.impl;

import com.mtlaa.mtchat.utils.sensitive.SensitiveWordFilter;

import java.util.*;

public class ACFilter implements SensitiveWordFilter {
    private final static Set<Character> skipSet = new HashSet<>(); // 遇到这些字符就会跳过
    static {
        for (char c : skipChars.toCharArray()) {
            skipSet.add(c);
        }
    }

    private static final ACTrieNode root = new ACTrieNode();

    public static class ACTrieNode{
        private final Map<Character, ACTrieNode> next;
        private boolean isEnd;
        private ACTrieNode failover;
        // Trie树中，一个节点 A 的fail指针指向节点 B，意味着 从root到节点B的字符串 是 root到节点A的字符串的一个后缀
        // 比如敏感词： 你妈的啊 和 妈的b  ，当遍历到‘的’时，满足上述情况
        // 所以，当遍历到节点 A，节点A后面的字符不匹配时，可以直接转到节点 B 继续匹配一个新的敏感词，而不是从root重新开始匹配

        /**
         * 用于表示从 root 开始，到当前节点字符串的长度（注意：该长度包含了特殊字符，不是节点个数）
         */
        private int depth;

        public ACTrieNode(){
            next = new HashMap<>();
            isEnd = false;
            failover = null;
        }

        public void addChildIfAbsent(char c){
            next.computeIfAbsent(c, a -> new ACTrieNode());
        }
        public ACTrieNode childOf(char c){
            return next.get(c);
        }
        public boolean hasChild(char c){
            return next.containsKey(c);
        }
    }


    @Override
    public boolean hasSensitiveWord(String text) {
        if (Objects.isNull(text)) return false;
        return filter(text).equals(text);
    }

    /**
     * 过滤字符串
     * @param text 文本
     */
    @Override
    public String filter(String text) {
        StringBuilder ret = new StringBuilder(text);
        int start = 0;

        while (start < ret.length()){
            char c = Character.toLowerCase(ret.charAt(start));
            if (skipSet.contains(c)){
                start++;
                continue;
            }
            ACTrieNode prev = root;
            for (int i = start; i < ret.length(); i++) {
                c = Character.toLowerCase(ret.charAt(i));
                if (skipSet.contains(c)) {
                    continue;
                }
                // 当前字符匹配成功, 转到当前字符对应的node
                if (prev.hasChild(c)){
                    prev = prev.childOf(c);
                } else {  // 当前字符匹配失败, 通过fail指针快速转移到另一个节点（root或者字符与当前字符相同的节点）
                    prev = prev.failover;
                    if (prev == null){ // 如果是根节点就匹配失败，说明没有以当前字符开头的敏感词，跳过
                        start = i;
                        break;
                    }
                    i--;  // 转移后，重新判断当前字符
                    continue;
                }
                // 找到一个敏感词, 把字符替换为 *
                if (prev.isEnd){
                    // 在 ret 中，该找到的敏感词可能包含特殊字符
                    // 通过 count 计数，确保所有敏感字符都被替换
                    int j = i, count = 0;
                    while (count < prev.depth){  // 从后往前，只有替换了敏感字符才统计数量。prev.depth为敏感词的长度
                        if (!skipSet.contains(ret.charAt(j))){
                            count++;
                        }
                        ret.setCharAt(j--, replace);
                    }
                    start = i;
                }
            }
            start++;
        }
        return ret.toString();
    }

    /**
     * 构建前缀树，然后初始化失败指针
     * @param words 敏感词数组
     */
    @Override
    public void loadWord(List<String> words) {
        for (String word : words){
            addWord(word);
        }
        initFailover();
    }

    /**
     * 初始化失败指针。AC自动机初始化失败指针一般使用层序遍历
     * <a href="https://blog.csdn.net/bestsort/article/details/82947639">AC自动机图解</a>
     * <a href="https://www.cnblogs.com/hyfhaha/p/10802604.html">AC自动机详解</a>
     */
    private void initFailover() {
        Queue<ACTrieNode> queue = new ArrayDeque<>();
        // 第一层节点的失败指针为 root，因为一个字符的后缀只能是它自己，可以看作没有后缀，从 root 重新开始匹配
        for (ACTrieNode node : root.next.values()){
            node.failover = root;
            queue.offer(node);
        }

        // 层序遍历
        while (!queue.isEmpty()){
            ACTrieNode parent = queue.poll();
            // 处理当前遍历节点的所有子节点
            for (Map.Entry<Character, ACTrieNode> entry : parent.next.entrySet()){
                ACTrieNode cur = entry.getValue();
                ACTrieNode fail = parent.failover;
                // 在树中找到 root->cur 字符串的一个后缀 root->node, 其中 fail 是 node 的父节点
                while (fail!=null && !fail.hasChild(entry.getKey())){
                    fail = fail.failover;
                }

                if (fail == null){
                    cur.failover = root;
                } else {
                    cur.failover = fail.childOf(entry.getKey());
                }
                queue.offer(cur);
            }
        }

    }

    /**
     * 把当前 word 添加到前缀树
     * @param word 敏感词
     */
    private void addWord(String word) {
        ACTrieNode prev = root;
        int depth = 0;
        for (int i = 0; i < word.length(); i++) {
            // 统一转小写
            char c = Character.toLowerCase(word.charAt(i));
            if (skipSet.contains(c)){  // 跳过特殊字符
                continue;
            }
            prev.addChildIfAbsent(c);  // 尝试添加当前字符的节点
            prev = prev.childOf(c);  // 获取当前字符的节点

            depth++;   // 当前节点的深度（跳过的字符不计）
            prev.depth = depth;
        }
        // 最后一个字符的节点设置结束
        if (prev != root){
            prev.isEnd = true;
        }
    }

}

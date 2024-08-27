package com.mtlaa.mtchat;

import com.mtlaa.mtchat.utils.sensitive.impl.ACFilter;
import com.mtlaa.mtchat.utils.sensitive.impl.DFAFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;


@Slf4j
public class OtherTest {
    @Test
    public void test1() throws InterruptedException {
        System.out.println(Arrays.toString(sortArray(new int[]{5,2,3,1})));
    }

    public int[] sortArray(int[] nums) {
        buildMaxHeap(nums);
        System.out.println(Arrays.toString(nums));
        for(int i=1;i<=nums.length;i++){
            swap(nums, 0, nums.length-i);
            adjust(nums, 0, nums.length-i);
            System.out.println(Arrays.toString(nums));
        }
        return nums;
    }

    private void buildMaxHeap(int[] nums){
        int index = nums.length / 2;
        for(;index>=0;index--){
            adjust(nums, index, nums.length);
        }
    }
    private void adjust(int[] nums, int i, int bound){
        int left = i * 2 + 1;
        int right = i * 2 + 2;
        if(left>=bound||right>=bound) return;
        int index = i;
        if(nums[left]>nums[index]){
            index = left;
        }
        if(nums[right]>nums[index]){
            index = right;
        }
        if(index!=i){
            swap(nums, index, i);
            adjust(nums, index, bound);
        }
    }
    private void swap(int[] nums, int i, int j){
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }


}

class MinStack {
    Deque<Integer> data;
    Deque<Integer> minStack;

    public MinStack() {
        data = new ArrayDeque<>();
        minStack = new ArrayDeque<>();
    }

    public void push(int val) {
        data.push(val);
        if (minStack.isEmpty() || val <= minStack.peek()){
            minStack.push(val);
        }
    }

    public void pop() {
        int val = data.pop();
        if (!minStack.isEmpty() && val == minStack.peek()){
            minStack.pop();
        }
    }

    public int top() {
        return data.peek();
    }

    public int getMin() {
        return minStack.peek();
    }
}

class ListNode {
    int val;
    ListNode next;
    public ListNode() {}
    public ListNode(int val) { this.val = val; }
    public ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}

class TreeNode {
      int val;
      TreeNode left;
      TreeNode right;
      TreeNode() {}
      TreeNode(int val) { this.val = val; }
      TreeNode(int val, TreeNode left, TreeNode right) {
          this.val = val;
          this.left = left;
          this.right = right;
      }
  }
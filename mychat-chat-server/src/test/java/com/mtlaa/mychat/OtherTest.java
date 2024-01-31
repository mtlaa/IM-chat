package com.mtlaa.mychat;

import com.mtlaa.mychat.common.utils.sensitiveWord.DFAFilter;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * Create 2023/12/12 14:37
 */
public class OtherTest {
    @Test
    public void testArraycopy(){
        DFAFilter dfaFilter = new DFAFilter();
        List<String> sensitiveWords = Arrays.asList("abcd", "abcbba", "adabca");
        dfaFilter.loadWord(sensitiveWords);
        System.out.println(dfaFilter.filter("as"));

    }
    @Test
    public void test(){
        int[] nums = new int[]{-1,3,3,3,3,12};
        System.out.println(search(nums, 3));
    }

    public int search(int[] nums, int target) {
        int left = 0, right = nums.length-1;
        while(left<right){
            int mid = left + (right-left)/2;
            if(nums[mid]==target){
                right = mid;
            }else if(nums[mid]<target){
                left = mid + 1;
            }else{
                right = mid - 1;
            }
        }
        return left;
    }

    @Test
    public void testMySqrt(){
//        int i = mySqrt(8);

        Deque<Integer> deque = new ArrayDeque<>();
        deque.addLast(1);
        deque.addLast(2222);
        System.out.println(deque);


    }
    public int mySqrt(int x) {
        int left = 0, right = x;
        while(left<=right){
            int mid = left + (right-left)/2;
            long value = (long) mid *mid;
            if(value==x){
                return mid;
            }else if(value>x){
                right = mid - 1;
            }else{
                left = mid + 1;
            }
        }
        return right;
    }
    public double mySqrtDouble(int x) {
        double left = 0, right = x;
        while(left<=right){
            double mid = left + (right-left)/2;
            double value = mid * mid;
            if(value==x){
                return mid;
            }else if(value>x){
                right = mid - 1e-10;
            }else{
                left = mid + 1e-10;
            }
        }
        return right;
    }

}

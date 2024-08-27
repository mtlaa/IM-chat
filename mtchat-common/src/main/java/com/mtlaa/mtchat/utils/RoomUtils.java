package com.mtlaa.mtchat.utils;


import com.mtlaa.mtchat.exception.BusinessException;

/**
 * Create 2023/12/21 19:59
 */
public class RoomUtils {
    public static String generateRoomKey(Long uid1, Long uid2){
        if(uid1.equals(uid2)){
            throw new BusinessException("不能与自己建立房间");
        }
        if(uid1.compareTo(uid2) < 0){
            return uid1 + "," + uid2;
        }
        return uid2 + "," + uid1;
    }
}

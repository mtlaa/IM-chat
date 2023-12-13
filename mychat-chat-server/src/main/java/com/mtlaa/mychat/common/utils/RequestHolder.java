package com.mtlaa.mychat.common.utils;

import com.mtlaa.mychat.common.domain.dto.RequestInfo;

/**
 * Create 2023/12/11 17:25
 */
public class RequestHolder {
    private final static ThreadLocal<RequestInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(RequestInfo requestInfo){
        THREAD_LOCAL.set(requestInfo);
    }
    public static RequestInfo get(){
        return THREAD_LOCAL.get();
    }
    public static void remove(){
        THREAD_LOCAL.remove();
    }
}

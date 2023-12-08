package com.mtlaa.mychat.common.constant;

import org.apache.commons.lang3.StringUtils;

/**
 * Create 2023/12/6 20:45
 */
public class RedisKey {
    public static final String BASE_KEY = "mychat:";

    public static final String USER_TOKEN_KEY = "userToken:uid_%d";

    public static String getKey(Object o){
        return BASE_KEY + String.format(USER_TOKEN_KEY, o);
    }
}

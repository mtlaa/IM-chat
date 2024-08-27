package com.mtlaa.mtchat.constant;

/**
 * Create 2023/12/6 20:45
 */
public class RedisKey {
    public static final String BASE_KEY = "mtchat:";

    public static final String USER_TOKEN_KEY = "userToken:uid_%d";

    /**
     * 在线用户列表
     */
    public static final String ONLINE_UID_ZET = "online";

    /**
     * 离线用户列表
     */
    public static final String OFFLINE_UID_ZET = "offline";

    /**
     * 热门房间列表
     */
    public static final String HOT_ROOM_ZET = "hotRoom";

    /**
     * 在线数量
     */
    public static final String USER_ONLINE_SET = "userOnlineSet";

    /**
     * 消息缓存
     */
    public static final String MSG_INFO_STRING = "messageInfo:msgId_%d";

    /**
     * 用户信息
     */
    public static final String USER_INFO_STRING = "userInfo:uid_%d";

    /**
     * 房间详情
     */
    public static final String ROOM_INFO_STRING = "roomInfo:roomId_%d";

    /**
     * 群组详情
     */
    public static final String GROUP_INFO_STRING = "groupInfo:roomId_%d";

    /**
     * 好友信息
     */
    public static final String FRIEND_INFO_STRING = "friendInfo:roomId_%d";

    /**
     * 黑名单缓存-uid
     */
    public static final String BLACK_USER_UID_SET = "blackUser:uid";
    /**
     * 黑名单缓存-ip
     */
    public static final String BLACK_USER_IP_SET = "blackUser:ip";

    /**
     * 用户的信息更新时间
     */
    public static final String USER_MODIFY_STRING = "userModify:uid_%d";

    /**
     * 用户的信息汇总
     */
    public static final String USER_SUMMARY_STRING = "userSummary:uid_%d";

    /**
     * 用户GPT聊天次数
     */
    public static final String USER_CHAT_NUM = "useChatGPTNum:uid_%d";

    public static final String USER_CHAT_CONTEXT = "useChatGPTContext:uid_%d_roomId_%d";

    /**
     * 保存Open id
     */
    public static final String OPEN_ID_STRING = "openid:%s";

    public static String getKey(String key, Object... objects) {
        return BASE_KEY + String.format(key, objects);
    }
}

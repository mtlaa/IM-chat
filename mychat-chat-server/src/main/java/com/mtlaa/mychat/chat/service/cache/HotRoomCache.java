package com.mtlaa.mychat.chat.service.cache;

import cn.hutool.core.lang.Pair;
import com.mtlaa.mychat.common.constant.RedisKey;
import com.mtlaa.mychat.common.domain.vo.request.CursorPageBaseReq;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;
import com.mtlaa.mychat.common.utils.CursorUtils;
import com.mtlaa.redis.utils.RedisUtils;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Set;

/**
 * Create 2023/12/28 17:23
 */
@Component
public class HotRoomCache {
    /**
     * 更新热门群聊的最新时间
     */
    public void refreshActiveTime(Long roomId, Date refreshTime){
        RedisUtils.zAdd(RedisKey.getKey(RedisKey.HOT_ROOM_ZET), roomId,
                (double) refreshTime.toInstant().toEpochMilli());
    }
    /**
     * 获取热门群聊，游标翻页
     */
    public CursorPageBaseResp<Pair<Long, Double>> getRoomCursorPage(CursorPageBaseReq pageBaseReq){
        return CursorUtils.getCursorPageByRedis(pageBaseReq, RedisKey.getKey(RedisKey.HOT_ROOM_ZET), Long::parseLong);
    }
    /**
     * score存储的是active_time
     */
    public Set<ZSetOperations.TypedTuple<String>> getRoomRange(Double hotStart, Double hotEnd) {
        return RedisUtils.zRangeByScoreWithScores(RedisKey.getKey(RedisKey.HOT_ROOM_ZET), hotStart, hotEnd);
    }
}

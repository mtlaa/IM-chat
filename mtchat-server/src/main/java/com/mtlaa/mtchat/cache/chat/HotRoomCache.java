package com.mtlaa.mtchat.cache.chat;

import cn.hutool.core.lang.Pair;

import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.utils.cursor.CursorUtils;
import com.mtlaa.mtchat.utils.redis.RedisUtils;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

/**
 * Redis缓存热点群聊的消息时间： <BR/>
 *      redis key = hotRoom  <BR/>
 *      value = roomId : active_time  <BR/>
 * -----------------------------  <BR/>
 * 使用zSet存储，因此可以根据 active_time（作为score）快速排序、游标翻页
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
     * @return 一页，其中每条数据为 [hotRoomId, activeTime]
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

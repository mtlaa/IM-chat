package com.mtlaa.mychat.chat.dao;

import com.mtlaa.mychat.chat.domain.entity.Room;
import com.mtlaa.mychat.chat.mapper.RoomMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Service
public class RoomDao extends ServiceImpl<RoomMapper, Room> {

    public void refreshActiveTime(Long roomId, Long msgId, Date createTime) {
        lambdaUpdate().eq(Room::getId, roomId)
                .lt(Room::getLastMsgId, msgId)
                .set(Room::getLastMsgId, msgId)
                .set(Room::getActiveTime, createTime)
                .update();
        // 有可能消息的消费顺序会不同于消息的发送顺序，即 有消息 1 2 3 4，先消费了4，再消费3，我们不能把最新消息id又改成3
    }
}

package com.mtlaa.mychat.chat.dao;

import com.mtlaa.mychat.chat.domain.entity.Room;
import com.mtlaa.mychat.chat.mapper.RoomMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}

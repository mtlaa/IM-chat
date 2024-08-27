package com.mtlaa.mtchat.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 消息表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

}

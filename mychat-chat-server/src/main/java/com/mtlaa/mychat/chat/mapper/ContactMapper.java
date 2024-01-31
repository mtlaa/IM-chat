package com.mtlaa.mychat.chat.mapper;

import com.mtlaa.mychat.chat.domain.entity.Contact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话列表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Mapper
public interface ContactMapper extends BaseMapper<Contact> {

    void refreshOrCreateActiveTime(Long roomId, List<Long> memberUidList, Long msgId, Date createTime);
}

package com.mtlaa.mtchat.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtlaa.mtchat.domain.chat.entity.Contact;
import org.apache.ibatis.annotations.Mapper;

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
    void refreshActiveTimeOrCreateContact(Long roomId, List<Long> memberUidList, Long msgId, Date createTime);
}

package com.mtlaa.mychat.chat.dao;

import com.mtlaa.mychat.chat.domain.entity.Contact;
import com.mtlaa.mychat.chat.mapper.ContactMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtlaa.mychat.common.domain.vo.request.CursorPageBaseReq;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;
import com.mtlaa.mychat.common.utils.CursorUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话列表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Service
public class ContactDao extends ServiceImpl<ContactMapper, Contact> {

    public void refreshOrCreateContact(Long roomId, List<Long> memberUidList, Long msgId, Date createTime) {
        baseMapper.refreshOrCreateActiveTime(roomId, memberUidList, msgId, createTime);
    }

    public Contact getByRoomIdAndUid(Long roomId, Long uid) {
        return lambdaQuery().eq(Contact::getRoomId, roomId)
                .eq(Contact::getUid, uid)
                .one();
    }

    public CursorPageBaseResp<Contact> getPage(CursorPageBaseReq request, Long uid) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(Contact::getUid, uid);
        }, Contact::getActiveTime);
    }

    public List<Contact> listByRoomIds(List<Long> roomIds, Long uid) {
        return lambdaQuery()
                .eq(Contact::getUid, uid)
                .in(Contact::getRoomId, roomIds)
                .list();
    }
}

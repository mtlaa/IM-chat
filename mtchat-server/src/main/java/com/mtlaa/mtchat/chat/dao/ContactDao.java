package com.mtlaa.mtchat.chat.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mtlaa.mtchat.chat.mapper.ContactMapper;
import com.mtlaa.mtchat.domain.chat.entity.Contact;
import com.mtlaa.mtchat.domain.chat.vo.request.ChatMessageReadReq;
import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.utils.cursor.CursorUtils;
import org.springframework.stereotype.Service;

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

    /**
     *
     * @param roomId
     * @param memberUidList
     * @param msgId
     * @param createTime
     */
    public void refreshOrCreateContact(Long roomId, List<Long> memberUidList, Long msgId, Date createTime) {
        // 创建会话，或者更新会话的消息时间
        baseMapper.refreshActiveTimeOrCreateContact(roomId, memberUidList, msgId, createTime);
    }

    public Contact getByRoomIdAndUid(Long roomId, Long uid) {
        return lambdaQuery().eq(Contact::getRoomId, roomId)
                .eq(Contact::getUid, uid)
                .one();
    }

    /**
     * 游标翻页，返回一页会话
     */
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

    public Integer countRead(Long roomId, Long sendUid, Date createTime) {
        return lambdaQuery().eq(Contact::getRoomId, roomId)
                .ne(Contact::getUid, sendUid)
                .gt(Contact::getReadTime, createTime)
                .count();
    }

    public Integer countUnread(Long roomId, Long sendUid, Date createTime) {
        return lambdaQuery().eq(Contact::getRoomId, roomId)
                .ne(Contact::getUid, sendUid)
                .lt(Contact::getReadTime, createTime)
                .count();
    }

    public CursorPageBaseResp<Contact> getReadPage(CursorPageBaseReq request, Long roomId, Long sendUid, Date createTime) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(Contact::getRoomId, roomId);
            wrapper.ne(Contact::getUid, sendUid);
            wrapper.gt(Contact::getReadTime, createTime);
            wrapper.select(Contact::getUid);  // 这样节省带宽是否有问题？   没有
        }, Contact::getReadTime);
    }

    public CursorPageBaseResp<Contact> getUnreadPage(CursorPageBaseReq request, Long roomId, Long sendUid, Date createTime) {
        return CursorUtils.getCursorPageByMysql(this, request, wrapper -> {
            wrapper.eq(Contact::getRoomId, roomId);
            wrapper.ne(Contact::getUid, sendUid);
            wrapper.lt(Contact::getReadTime, createTime);
            wrapper.select(Contact::getUid);  // 这样节省带宽是否有问题？   没有
        }, Contact::getReadTime);
    }

    /**
     * 删除指定room里指定用户的会话
     * @param roomId 房间
     * @param uidList 指定用户，为空则删除所有
     */
    public void removeByRoomId(Long roomId, List<Long> uidList) {
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Contact::getRoomId, roomId);
        if (!CollectionUtil.isEmpty(uidList)){
            wrapper.in(Contact::getUid, uidList);
        }
        remove(wrapper);
    }
}

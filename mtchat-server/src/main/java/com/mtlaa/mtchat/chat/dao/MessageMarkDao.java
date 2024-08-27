package com.mtlaa.mtchat.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtlaa.mtchat.chat.mapper.MessageMarkMapper;
import com.mtlaa.mtchat.domain.chat.entity.MessageMark;
import com.mtlaa.mtchat.domain.common.enums.NormalOrNoEnum;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageMarkDao extends ServiceImpl<MessageMarkMapper, MessageMark> {

    public List<MessageMark> listByMsgIdAndValid(List<Long> msgIds) {
        return lambdaQuery().eq(MessageMark::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .in(MessageMark::getMsgId, msgIds)
                .list();
    }

    public MessageMark getByUidAndMsgId(Long uid, Long msgId) {
        return lambdaQuery().eq(MessageMark::getUid, uid)
                .eq(MessageMark::getMsgId, msgId)
                .one();
    }
}

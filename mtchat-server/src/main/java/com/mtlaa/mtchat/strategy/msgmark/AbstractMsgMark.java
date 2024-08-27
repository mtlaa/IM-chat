package com.mtlaa.mtchat.strategy.msgmark;

import com.mtlaa.mtchat.chat.dao.MessageMarkDao;
import com.mtlaa.mtchat.domain.chat.entity.MessageMark;
import com.mtlaa.mtchat.domain.chat.enums.MessageMarkTypeEnum;
import com.mtlaa.mtchat.domain.common.enums.NormalOrNoEnum;
import com.mtlaa.mtchat.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Objects;

public abstract class AbstractMsgMark {
    @Autowired
    private MessageMarkDao messageMarkDao;

    @PostConstruct
    public void init(){
        MsgMarkFactory.register(this, getType());
    }
    public abstract MessageMarkTypeEnum getType();
    protected abstract void doMark(MessageMark messageMark);
    protected abstract void doUnMark(MessageMark messageMark);

    /**
     * 确认标记
     */
    public void mark(Long uid, Long msgId){
        MessageMark messageMark = getMsgMark(uid, msgId);
        doMark(messageMark);
        saveOrUpdateMsgMark(messageMark);
    }

    /**
     * 取消标记
     */
    public void unMark(Long uid, Long msgId){
        MessageMark messageMark = getMsgMark(uid, msgId);
        doUnMark(messageMark);
        saveOrUpdateMsgMark(messageMark);
    }

    /**
     * 查询 或 构建 messageMark，构建的话只设置通用信息
     * @param uid uid
     * @param msgId msgId
     * @return MessageMark
     */
    private MessageMark getMsgMark(Long uid, Long msgId){
        MessageMark messageMark = messageMarkDao.getByUidAndMsgId(uid, msgId);
        if (Objects.isNull(messageMark)){
            messageMark = new MessageMark();
            messageMark.setMsgId(msgId);
            messageMark.setUid(uid);
            messageMark.setCreateTime(new Date());
        }
        return messageMark;
    }

    /**
     * 更新或保存消息标记
     * @param messageMark 消息标记
     */
    private void saveOrUpdateMsgMark(MessageMark messageMark){
        messageMark.setUpdateTime(new Date());
        messageMarkDao.saveOrUpdate(messageMark);
    }

    /**
     * 校验操作前是否是期望的状态
     * @param messageMark 操作前的状态
     * @param typeEnum 期望是 点赞 or 点踩
     * @param actTypeEnum 期望是 确认 or 取消
     */
    protected void assertMark(MessageMark messageMark, MessageMarkTypeEnum typeEnum, NormalOrNoEnum status){
        if (Objects.isNull(messageMark.getType()) && Objects.isNull(messageMark.getStatus())){
            return;
        }
        if (!messageMark.getType().equals(typeEnum.getType()) || !messageMark.getStatus().equals(status.getStatus())){
            throw new BusinessException("消息标记参数与预期不符");
        }
    }
}

package com.mtlaa.mtchat.strategy.msghandler;

import cn.hutool.core.bean.BeanUtil;

import com.mtlaa.mtchat.chat.adapter.MessageAdapter;
import com.mtlaa.mtchat.chat.dao.MessageDao;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.ChatMessageReq;
import com.mtlaa.mtchat.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;

/**
 * Create 2023/12/25 15:11
 */
public abstract class AbstractMsgHandler<Req> {
    private Class<Req> bodyClass;
    @Autowired
    private MessageDao messageDao;

    @PostConstruct
    private void init() {
        // 取出泛型类型的代码
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.bodyClass = (Class<Req>) genericSuperclass.getActualTypeArguments()[0];
        // 注册工厂
        MsgHandlerFactory.register(getMsgTypeEnum().getType(), this);
    }

    /**
     * 获取策略的类型
     */
    abstract MessageTypeEnum getMsgTypeEnum();

    /**
     * 因为某些消息类型不需要额外检查，所以该方法有默认空实现
     */
    protected void checkMsg(Req body, Long roomId, Long uid) {}

    /**
     * 策略接口，根据消息类型检查并保存消息
     * @param request 发送消息的请求体
     * @param uid 发送消息的用户
     * @return 消息的入库id，msgId
     */
    @Transactional
    public Long checkAndSaveMsg(ChatMessageReq request, Long uid) {
        Req body = this.toBean(request.getBody());
        //统一校验：校验字段值是否符合实体类属性上的注解约束
        AssertUtil.allCheckValidateThrow(body);
        //子类扩展校验：根据类型，子类有不同的校验方式，如文本的敏感词
        checkMsg(body, request.getRoomId(), uid);
        Message insert = MessageAdapter.buildMsgSave(request, uid);  // 先保存一些通用信息
        //统一保存
        messageDao.save(insert);
        //子类扩展保存
        saveMsg(insert, body);
        return insert.getId();
    }

    private Req toBean(Object body) {
        if (bodyClass.isAssignableFrom(body.getClass())) {
            return (Req) body;
        }
        return BeanUtil.toBean(body, bodyClass);
    }

    /**
     * 保存消息的消息体以及额外信息
     * @param message 消息的通用信息
     * @param body 消息体以及额外信息
     */
    protected abstract void saveMsg(Message message, Req body);

    /**
     * 会话内——展示消息
     * @param msg 消息保存体
     * @return 消息展示体
     */
    public abstract Object showMsg(Message msg);

    /**
     * 被回复时——展示的消息
     */
    public abstract Object showReplyMsg(Message msg);

    /**
     * 会话列表——展示的消息
     */
    public abstract String showContactMsg(Message msg);
}

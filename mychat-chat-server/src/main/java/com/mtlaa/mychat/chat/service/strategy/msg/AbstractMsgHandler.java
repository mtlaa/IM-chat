package com.mtlaa.mychat.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import com.mtlaa.mychat.chat.dao.MessageDao;
import com.mtlaa.mychat.chat.domain.entity.Message;
import com.mtlaa.mychat.chat.domain.enums.MessageTypeEnum;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessageReq;
import com.mtlaa.mychat.chat.service.adapter.MessageAdapter;
import com.mtlaa.mychat.common.utils.AssertUtil;
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

    abstract MessageTypeEnum getMsgTypeEnum();

    protected void checkMsg(Req body, Long roomId, Long uid) {

    }

    @Transactional
    public Long checkAndSaveMsg(ChatMessageReq request, Long uid) {
        Req body = this.toBean(request.getBody());
        //统一校验
        AssertUtil.allCheckValidateThrow(body);
        //子类扩展校验
        checkMsg(body, request.getRoomId(), uid);
        Message insert = MessageAdapter.buildMsgSave(request, uid);
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

    protected abstract void saveMsg(Message message, Req body);

    /**
     * 展示消息
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

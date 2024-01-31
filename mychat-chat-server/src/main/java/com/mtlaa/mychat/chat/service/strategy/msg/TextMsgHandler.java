package com.mtlaa.mychat.chat.service.strategy.msg;

import cn.hutool.core.collection.CollectionUtil;

import com.mtlaa.mychat.chat.dao.MessageDao;
import com.mtlaa.mychat.chat.domain.entity.Message;
import com.mtlaa.mychat.chat.domain.entity.msg.MessageExtra;
import com.mtlaa.mychat.chat.domain.enums.MessageStatusEnum;
import com.mtlaa.mychat.chat.domain.enums.MessageTypeEnum;
import com.mtlaa.mychat.chat.domain.vo.request.msg.TextMsgReq;
import com.mtlaa.mychat.chat.domain.vo.response.msg.TextMsgResp;
import com.mtlaa.mychat.chat.service.adapter.MessageAdapter;
import com.mtlaa.mychat.common.exception.BusinessException;
import com.mtlaa.mychat.common.utils.discover.PrioritizedUrlDiscover;
import com.mtlaa.mychat.common.utils.discover.domain.UrlInfo;
import com.mtlaa.mychat.common.utils.sensitiveWord.SensitiveWordBs;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.domain.enums.RoleEnum;
import com.mtlaa.mychat.user.service.RoleService;
import com.mtlaa.mychat.user.service.cache.UserCache;
import com.mtlaa.mychat.user.service.cache.UserInfoCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description: 普通文本消息
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-04
 */
@Component
public class TextMsgHandler extends AbstractMsgHandler<TextMsgReq> {
    @Autowired
    private MessageDao messageDao;
//    @Autowired
//    private MsgCache msgCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private RoleService roleService;
    @Autowired
    private SensitiveWordBs sensitiveWordBs;
//
    private static final PrioritizedUrlDiscover URL_TITLE_DISCOVER = new PrioritizedUrlDiscover();

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.TEXT;
    }

    @Override
    protected void checkMsg(TextMsgReq body, Long roomId, Long uid) {
        //校验下回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            Message replyMsg = messageDao.getById(body.getReplyMsgId());
            if(replyMsg == null){
                throw new BusinessException("回复消息不存在");
            }
            if(!roomId.equals(replyMsg.getRoomId())){
                throw new BusinessException("只能回复相同会话内的消息");
            }
        }
        // 校验 @
        if (CollectionUtil.isNotEmpty(body.getAtUidList())) {
            //前端传入的@用户列表可能会重复，需要去重
            List<Long> atUidList = body.getAtUidList().stream().distinct().collect(Collectors.toList());
            Map<Long, User> batch = userInfoCache.getBatch(atUidList);
            //如果@用户不存在，userInfoCache 返回的map中依然存在该key，但是value为null，需要过滤掉再校验
            long batchCount = batch.values().stream().filter(Objects::nonNull).count();
            if((long)atUidList.size() != batchCount){
                throw new BusinessException("@用户不存在");
            }
            boolean atAll = body.getAtUidList().contains(0L);
            if (atAll) {
                if (!roleService.hasPower(uid, RoleEnum.CHAT_MANAGER)) {
                    throw new BusinessException("没有权限");
                }
            }
        }
    }

    @Override
    public void saveMsg(Message msg, TextMsgReq body) {//插入文本内容
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setContent(sensitiveWordBs.filter(body.getContent()));
        update.setExtra(extra);
        //如果有回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            Integer gapCount = messageDao.getGapCount(msg.getRoomId(), body.getReplyMsgId(), msg.getId());
            update.setGapCount(gapCount);
            update.setReplyMsgId(body.getReplyMsgId());
        }
        //判断消息url跳转
        Map<String, UrlInfo> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(body.getContent());
        extra.setUrlContentMap(urlContentMap);
        //艾特功能
        if (CollectionUtil.isNotEmpty(body.getAtUidList())) {
            extra.setAtUidList(body.getAtUidList());
        }

        messageDao.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        TextMsgResp resp = new TextMsgResp();
        resp.setContent(msg.getContent());
        Message replyMsg = null;
        // 设置回复消息
        if(Objects.nonNull(msg.getReplyMsgId())){
            replyMsg = messageDao.getById(msg.getReplyMsgId());
            if(replyMsg.getStatus().equals(MessageStatusEnum.DELETE.getStatus())){
                replyMsg = null;
            }
        }
        if(Objects.nonNull(replyMsg)){
            TextMsgResp.ReplyMsg replyMsgVo = new TextMsgResp.ReplyMsg();
            replyMsgVo.setId(replyMsg.getId());
            replyMsgVo.setUid(replyMsg.getFromUid());
            replyMsgVo.setType(replyMsg.getType());
            replyMsgVo.setBody(MsgHandlerFactory.getStrategyNoNull(replyMsg.getType()).showReplyMsg(replyMsg));
            replyMsgVo.setUsername(userInfoCache.get(replyMsg.getFromUid()).getName());
            replyMsgVo.setCanCallback((Objects.nonNull(msg.getGapCount()) && msg.getGapCount() <= MessageAdapter.CAN_CALLBACK_GAP_COUNT)
                    ? 1 : 0);
            replyMsgVo.setGapCount(msg.getGapCount());
            resp.setReply(replyMsgVo);
        }
        resp.setUrlContentMap(Optional.ofNullable(msg.getExtra()).map(MessageExtra::getUrlContentMap).orElse(null));
        resp.setAtUidList(Optional.ofNullable(msg.getExtra()).map(MessageExtra::getAtUidList).orElse(null));
        return resp;
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return msg.getContent();
    }

    @Override
    public String showContactMsg(Message msg) {
        return msg.getContent();
    }
}

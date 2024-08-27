package com.mtlaa.mtchat.strategy.msghandler;

import cn.hutool.core.collection.CollectionUtil;

import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.cache.user.UserInfoCache;
import com.mtlaa.mtchat.chat.adapter.MessageAdapter;
import com.mtlaa.mtchat.chat.dao.MessageDao;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.msg.MessageExtra;
import com.mtlaa.mtchat.domain.chat.enums.MessageStatusEnum;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.msg.TextMsgReq;
import com.mtlaa.mtchat.domain.chat.vo.response.msg.TextMsgResp;
import com.mtlaa.mtchat.domain.urldiscover.UrlInfo;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.enums.RoleEnum;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.user.service.RoleService;
import com.mtlaa.mtchat.utils.sensitive.SensitiveWordFilter;
import com.mtlaa.mtchat.utils.urldiscover.PrioritizedUrlDiscover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Description: 普通文本消息
 */
@Component
public class TextMsgHandler extends AbstractMsgHandler<TextMsgReq> {
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private RoleService roleService;
    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    private static final PrioritizedUrlDiscover URL_TITLE_DISCOVER = new PrioritizedUrlDiscover();

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.TEXT;
    }

    /**
     * 校验文本消息：
     *      1、校验回复的消息存不存在，以及是否在当前房间下
     *      2、校验 @ 的群成员存不存在，是否@全员以及权限
     * @param body 消息体
     * @param roomId 房间id
     * @param uid 发送人
     */
    @Override
    protected void checkMsg(TextMsgReq body, Long roomId, Long uid) {
        //校验下回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            // 使用 messageCache 行
//            Message replyMsg = messageDao.getById(body.getReplyMsgId());
            Message replyMsg = msgCache.getMsg(body.getReplyMsgId());
            if(replyMsg == null){
                throw new BusinessException("回复消息不存在");
            }
            if(!roomId.equals(replyMsg.getRoomId())){
                throw new BusinessException("只能回复相同会话内的消息");
            }
        }
        // 校验 @ 用户
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

    /**
     * 保存文本消息（通用信息已经保存，这里实际上是更新）：
     *      1、保存消息文本内容，需要敏感词过滤
     *      2、保存回复的消息id，以及计算出当前消息与回复消息之间差多少条（用于前端实现消息跳转。先计算再入库，只用计算这一次）
     *      3、解析消息中的url成小卡片，保存到 extra额外信息中。（先解析再入库，只用解析这一次）
     *      4、保存 @ 群成员列表到 extra
     * @param msg 消息的通用信息
     * @param body 消息体以及额外信息
     */
    @Override
    public void saveMsg(Message msg, TextMsgReq body) {//插入文本内容
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        // 敏感词过滤
        update.setContent(sensitiveWordFilter.filter(body.getContent()));
        update.setExtra(extra);
        //如果有回复消息
        if (Objects.nonNull(body.getReplyMsgId())) {
            Integer gapCount = messageDao.getGapCount(msg.getRoomId(), body.getReplyMsgId(), msg.getId());
            update.setGapCount(gapCount);
            update.setReplyMsgId(body.getReplyMsgId());
        }
        // 消息url解析
        Map<String, UrlInfo> urlContentMap = URL_TITLE_DISCOVER.getUrlContentMap(body.getContent());
        extra.setUrlContentMap(urlContentMap);
        //艾特功能
        if (CollectionUtil.isNotEmpty(body.getAtUidList())) {
            extra.setAtUidList(body.getAtUidList());
        }

        msgCache.updateById(update);
    }

    /**
     * 会话内的消息展示
     * @param msg 消息保存体
     * @return 消息展示：1、未撤回：消息内容；2、已撤回：xx撤回一条消息；3、回复消息：消息展示体包含被回复的消息展示
     */
    @Override
    public Object showMsg(Message msg) {
        TextMsgResp resp = new TextMsgResp();
        resp.setContent(msg.getContent());
        Message replyMsg = null;
        // 设置回复消息
        if(Objects.nonNull(msg.getReplyMsgId())){
            replyMsg = msgCache.getMsg(msg.getReplyMsgId());
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

    /**
     * 消息被回复时的展示
     */
    @Override
    public Object showReplyMsg(Message msg) {
        return msg.getContent();
    }

    /**
     * 在会话列表消息的展示
     */
    @Override
    public String showContactMsg(Message msg) {
        return msg.getContent();
    }
}

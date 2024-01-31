package com.mtlaa.mychat.chat.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.mtlaa.mychat.chat.dao.ContactDao;
import com.mtlaa.mychat.chat.dao.GroupMemberDao;
import com.mtlaa.mychat.chat.dao.MessageDao;
import com.mtlaa.mychat.chat.dao.RoomFriendDao;
import com.mtlaa.mychat.chat.domain.entity.*;
import com.mtlaa.mychat.chat.domain.enums.MessageMarkActTypeEnum;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessageBaseReq;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessageMarkReq;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessagePageReq;
import com.mtlaa.mychat.chat.domain.vo.request.ChatMessageReq;
import com.mtlaa.mychat.chat.domain.vo.request.member.MemberReq;
import com.mtlaa.mychat.chat.domain.vo.response.ChatMemberResp;
import com.mtlaa.mychat.chat.domain.vo.response.ChatMessageResp;
import com.mtlaa.mychat.chat.service.ChatService;
import com.mtlaa.mychat.chat.service.adapter.MessageAdapter;
import com.mtlaa.mychat.chat.service.cache.RoomCache;
import com.mtlaa.mychat.chat.service.cache.RoomGroupCache;
import com.mtlaa.mychat.chat.service.strategy.msg.AbstractMsgHandler;
import com.mtlaa.mychat.chat.service.strategy.msg.MsgHandlerFactory;
import com.mtlaa.mychat.chat.service.strategy.msg.RecallMsgHandler;
import com.mtlaa.mychat.common.domain.enums.NormalOrNoEnum;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;
import com.mtlaa.mychat.common.event.MessageSendEvent;
import com.mtlaa.mychat.common.exception.BusinessException;
import com.mtlaa.mychat.user.domain.enums.BlackTypeEnum;
import com.mtlaa.mychat.user.domain.enums.RoleEnum;
import com.mtlaa.mychat.user.service.RoleService;
import com.mtlaa.mychat.user.service.cache.UserCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Create 2023/12/25 14:18
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private RoleService roleService;
    @Autowired
    private RecallMsgHandler recallMsgHandler;

    /**
     * 消息流程：  前端请求  -->  检查权限  -->  检查格式  -->  构造消息体  -->  消息入库
     *          -->  （发出消息发送事件）  -->  事件处理器：把消息推送到消息队列（只推送消息id）【发送队列】
     *          -->  消费者：MsgSendConsumer，构造消息返回体，再推送到消息队列【推送队列】（该消息队列包含WebSocket消息，用于让WS服务慢慢推送消息）
     *          -->  消费者：PushConsumer，把消息通过WebSocket连接进行推送  -->  webSocket推送
     */
    @Override
    @Transactional
    public Long sendMsg(Long uid, ChatMessageReq chatMessageReq) {
        // 检查是否能够在该房间发送消息
        check(uid, chatMessageReq);
        // 根据消息类型检查并持久化消息
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(chatMessageReq.getMsgType());
        Long msgId = msgHandler.checkAndSaveMsg(chatMessageReq, uid);  // 执行了消息入库
        // 发出消息发送事件，在事件处理中执行发送消息到MQ的一致性操作
        applicationEventPublisher.publishEvent(new MessageSendEvent(this, msgId));
        return msgId;
    }

    @Override
    public ChatMessageResp getMsgResponse(Message message, Long receivedUid) {
        // TODO 暂时还没有消息标记
        return MessageAdapter.buildMsgRespNoMark(message, receivedUid);
    }

    @Override
    public ChatMessageResp getMsgResponse(Long msgId, Long receivedUid) {
        // TODO 暂时还没有消息标记
        return MessageAdapter.buildMsgRespNoMark(messageDao.getById(msgId), receivedUid);
    }

    /**
     * 消息列表游标翻页查询
     */
    @Override
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq pageReq, Long uid) {
        Long lastMsgId =getLastMsgId(pageReq.getRoomId(), uid);
        CursorPageBaseResp<Message> msgPage = messageDao.getCursorPage(pageReq, pageReq.getRoomId(), lastMsgId);
        // 过滤掉黑名单用户发送的消息
        filterBlackMsg(msgPage);
        if(msgPage.isEmpty()){
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(msgPage, getMsgRespBatch(msgPage.getList(), uid));
    }

    @Override
    public void recallMsg(Long uid, ChatMessageBaseReq request) {
        Message message = messageDao.getById(request.getMsgId());
        // 检查是否能够撤回
        checkRecall(uid, message);
        // 执行撤回
        recallMsgHandler.recall(uid, message);
    }

    @Override
    public void setMsgMark(Long uid, ChatMessageMarkReq request) {
        return;
    }

    @Override
    public void msgRead(Long uid, Long roomId) {
        Contact contact = contactDao.getByRoomIdAndUid(roomId, uid);
        if (Objects.nonNull(contact)){
            Contact update = new Contact();
            update.setId(contact.getId());
            update.setReadTime(LocalDateTime.now());
            contactDao.updateById(update);
        } else {
            Contact insert = new Contact();
            insert.setUid(uid);
            insert.setRoomId(roomId);
            insert.setReadTime(LocalDateTime.now());
            contactDao.save(insert);
        }
    }


    private void checkRecall(Long uid, Message message) {
        boolean hasPower = roleService.hasPower(uid, RoleEnum.CHAT_MANAGER);
        if(hasPower){  // 如果有管理权限
            return;
        }
        if (!message.getFromUid().equals(uid)){
            throw new BusinessException("不是自己发送的消息");
        }
        if (DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE) > 2){
            throw new BusinessException("超过2分钟不能撤回");
        }
    }

    private void filterBlackMsg(CursorPageBaseResp<Message> msgPage) {
        Set<String> blackUidSet = userCache.getBlackMap().getOrDefault(BlackTypeEnum.UID.getType(), new HashSet<>());
        msgPage.getList().removeIf(message -> blackUidSet.contains(message.getFromUid().toString()));
    }

    private List<ChatMessageResp> getMsgRespBatch(List<Message> messages, Long uid) {
        if(CollectionUtil.isEmpty(messages)){
            return new ArrayList<>();
        }
        // TODO 暂时还没有消息标记 设置消息标志 mark
        return MessageAdapter.buildMsgRespNoMark(messages, uid);
    }

    private Long getLastMsgId(Long roomId, Long uid) {
        Room room = roomCache.get(roomId);
        if(room.isHotRoom()){
            return null;
        }
        Contact contact = contactDao.getByRoomIdAndUid(roomId, uid);
        if(contact == null){
            throw new BusinessException("无会话，错误查询");
        }
        return contact.getLastMsgId();
    }

    /**
     * 检查uid是否能在房间发送消息
     */
    private void check(Long uid, ChatMessageReq chatMessageReq){
        Room room = roomCache.get(chatMessageReq.getRoomId());
        if(room.isHotRoom()){
            return;
        }
        if(room.isRoomFriend()){
            RoomFriend roomFriend = roomFriendDao.getByRoomId(chatMessageReq.getRoomId());
            if(!uid.equals(roomFriend.getUid1()) && !uid.equals(roomFriend.getUid2())){
                throw new BusinessException("不属于你的聊天");
            }
            if(roomFriend.getStatus().equals(NormalOrNoEnum.NOT_NORMAL.getStatus())){
                throw new BusinessException("已被对方拉黑，无法发送");
            }
        }
        if(room.isRoomGroup()){
            RoomGroup roomGroup = roomGroupCache.get(chatMessageReq.getRoomId());
            GroupMember groupMember = groupMemberDao.getByUidAndGroupId(uid, roomGroup.getId());
            if(Objects.isNull(groupMember)){
                throw new BusinessException("你已被踢出群聊");
            }
        }
    }
}

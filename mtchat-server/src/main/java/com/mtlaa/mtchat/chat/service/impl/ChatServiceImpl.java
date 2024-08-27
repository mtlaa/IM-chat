package com.mtlaa.mtchat.chat.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.cache.chat.RoomCache;
import com.mtlaa.mtchat.cache.chat.RoomFriendCache;
import com.mtlaa.mtchat.cache.chat.RoomGroupCache;
import com.mtlaa.mtchat.cache.user.BlackCache;
import com.mtlaa.mtchat.cache.user.UserCache;
import com.mtlaa.mtchat.chat.adapter.MessageAdapter;
import com.mtlaa.mtchat.chat.dao.*;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.strategy.msghandler.AbstractMsgHandler;
import com.mtlaa.mtchat.strategy.msghandler.MsgHandlerFactory;
import com.mtlaa.mtchat.strategy.msghandler.RecallMsgHandler;
import com.mtlaa.mtchat.chat.service.ChatService;
import com.mtlaa.mtchat.strategy.msgmark.AbstractMsgMark;
import com.mtlaa.mtchat.strategy.msgmark.MsgMarkFactory;
import com.mtlaa.mtchat.domain.chat.dto.MsgReadInfoDTO;
import com.mtlaa.mtchat.domain.chat.entity.*;
import com.mtlaa.mtchat.domain.chat.enums.MessageMarkActTypeEnum;
import com.mtlaa.mtchat.domain.chat.enums.MessageMarkTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.*;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMessageReadResp;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMessageResp;
import com.mtlaa.mtchat.domain.common.enums.NormalOrNoEnum;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.domain.user.enums.BlackTypeEnum;
import com.mtlaa.mtchat.domain.user.enums.RoleEnum;
import com.mtlaa.mtchat.event.MessageSendEvent;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.user.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;



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
    private MsgCache msgCache;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private RoomFriendCache roomFriendCache;
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
    @Autowired
    private MessageMarkDao messageMarkDao;
    @Autowired
    private BlackCache blackCache;

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

    /**
     * 获取当个消息的展示。复用批量的方法
     */
    @Override
    public ChatMessageResp getMsgResponse(Message message, Long receivedUid) {
        return CollectionUtils.firstElement(getMsgRespBatch(Collections.singletonList(message), receivedUid));
    }

    /**
     * 获取单个消息的展示。复用批量的方法
     */
    @Override
    public ChatMessageResp getMsgResponse(Long msgId, Long receivedUid) {
        return getMsgResponse(msgCache.getMsg(msgId), receivedUid);
    }

    /**
     * 消息列表游标翻页查询
     */
    @Override
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq pageReq, Long uid) {
        Long lastMsgId = getLastMsgId(pageReq.getRoomId(), uid);
        CursorPageBaseResp<Message> msgPage = messageDao.getCursorPage(pageReq, pageReq.getRoomId(), lastMsgId);
        // 过滤掉黑名单用户发送的消息
        filterBlackMsg(msgPage);
        if(msgPage.isEmpty()){
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(msgPage, getMsgRespBatch(msgPage.getList(), uid));
    }

    /**
     * 撤回消息。
     * @param uid 撤回人
     * @param request 包含 roomId、msgId
     */
    @Override
    public void recallMsg(Long uid, ChatMessageBaseReq request) {
        Message message = msgCache.getMsg(request.getMsgId());
        // 检查是否能够撤回
        checkRecall(uid, message);
        // 执行撤回
        recallMsgHandler.recall(uid, message);
    }

    /**
     * 标记消息：点赞、点踩；确认、取消
     * @param uid 操作用户
     * @param request 目标消息id，点赞/点踩，确认/取消
     */
    @Override
    public void setMsgMark(Long uid, ChatMessageMarkReq request) {
        AbstractMsgMark msgMarkHandler = MsgMarkFactory.get(MessageMarkTypeEnum.of(request.getMarkType()));
        switch (MessageMarkActTypeEnum.of(request.getActType())){
            // 确认
            case MARK:
                msgMarkHandler.mark(uid, request.getMsgId());
                break;
            // 取消
            case UN_MARK:
                msgMarkHandler.unMark(uid, request.getMsgId());
        }
    }

    /**
     * 更新用户的最新阅读时间
     * @param uid uid
     * @param roomId roomId
     */
    @Override
    public void msgRead(Long uid, Long roomId) {
        Contact contact = contactDao.getByRoomIdAndUid(roomId, uid);
        if (Objects.nonNull(contact)){
            Contact update = new Contact();
            update.setId(contact.getId());
            update.setReadTime(LocalDateTime.now());
            update.setUpdateTime(LocalDateTime.now());
            contactDao.updateById(update);
        } else {
            Contact insert = new Contact();
            insert.setUid(uid);
            insert.setRoomId(roomId);
            insert.setReadTime(LocalDateTime.now());
            insert.setCreateTime(LocalDateTime.now());
            insert.setUpdateTime(LocalDateTime.now());
            contactDao.save(insert);
        }
    }

    /**
     * 查询消息的已读未读数
     * @param uid 当前用户
     * @param request 包含多个消息id
     * @return 消息已读未读数的列表
     */
    @Override
    public Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq request) {
        // 截断
        List<Long> subList = request.getMsgIds();
        if (subList.size() > 20)
            subList = request.getMsgIds().subList(request.getMsgIds().size() - 20, request.getMsgIds().size());
        Map<Long, Message> messageMap = msgCache.getBatch(subList);
        return subList.stream().map(msgId -> {
            Message message = messageMap.get(msgId);
            Integer readCount = contactDao.countRead(message.getRoomId(), uid, message.getCreateTime());
            Integer unreadCount = contactDao.countUnread(message.getRoomId(), uid, message.getCreateTime());
            return new MsgReadInfoDTO(msgId, readCount, unreadCount);
        }).collect(Collectors.toList());
    }


    /**
     * 查询消息的已读或未读列表。游标翻页应对频繁变化
     * @param uid 当前用户
     * @param request 消息id 以及 查询已读还是未读
     * @return 已读 或 未读 该消息的uid列表
     */
    @Override
    public CursorPageBaseResp<ChatMessageReadResp> getReadPage(Long uid, ChatMessageReadReq request) {
        Message message = msgCache.getMsg(request.getMsgId());
        CursorPageBaseResp<Contact> contacts;
        if (request.getSearchType().equals(1L)){  // 查询已读
            contacts = contactDao.getReadPage(request, message.getRoomId(), uid, message.getCreateTime());
        } else if(request.getSearchType().equals(2L)){  // 查询未读
            contacts = contactDao.getUnreadPage(request, message.getRoomId(), uid, message.getCreateTime());
        } else {
            throw new BusinessException("查询已读未读列表类型错误");
        }
        return CursorPageBaseResp.init(contacts, contacts.getList().stream()
                .map(contact -> new ChatMessageReadResp(contact.getUid())).collect(Collectors.toList()));
    }


    /**
     * 检查当前用户能否撤回这一条消息
     * @param uid uid
     * @param message 消息
     */
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

    /**
     * 过滤掉被拉黑用户发送的消息
     * @param msgPage 一页消息
     */
    private void filterBlackMsg(CursorPageBaseResp<Message> msgPage) {
        Set<String> blackUidSet = blackCache.getBlackMap().getOrDefault(BlackTypeEnum.UID.getType(), new HashSet<>());
        msgPage.getList().removeIf(message -> blackUidSet.contains(message.getFromUid().toString()));
    }

    /**
     * 批量生成消息的返回体
     * @param messages 输入消息
     * @param uid 接收人的id
     * @return 消息返回体的列表
     */
    private List<ChatMessageResp> getMsgRespBatch(List<Message> messages, Long uid) {
        if(CollectionUtil.isEmpty(messages)){
            return new ArrayList<>();
        }
        // 设置消息标志 mark
        List<MessageMark> messageMarks = messageMarkDao.listByMsgIdAndValid(
                messages.stream().map(Message::getId).collect(Collectors.toList()));
        return MessageAdapter.buildMsgResp(messages, messageMarks, uid);
    }

    /**
     * 获取当前用户在该房间内的最后一条消息id
     * @param roomId 房间
     * @param uid 当前用户
     * @return 最新消息id；如果是热点群聊，返回 null
     */
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
        if(room.isHotRoom() || uid.equals(User.SYSTEM_UID)){
            return;
        }
        if(room.isRoomFriend()){
            RoomFriend roomFriend = roomFriendCache.get(chatMessageReq.getRoomId());
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

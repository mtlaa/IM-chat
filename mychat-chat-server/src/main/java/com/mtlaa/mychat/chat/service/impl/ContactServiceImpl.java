package com.mtlaa.mychat.chat.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import com.mtlaa.mychat.chat.dao.ContactDao;
import com.mtlaa.mychat.chat.dao.GroupMemberDao;
import com.mtlaa.mychat.chat.dao.MessageDao;
import com.mtlaa.mychat.chat.dao.RoomFriendDao;
import com.mtlaa.mychat.chat.domain.dto.RoomBaseInfo;
import com.mtlaa.mychat.chat.domain.entity.*;
import com.mtlaa.mychat.chat.domain.enums.RoomTypeEnum;
import com.mtlaa.mychat.chat.domain.vo.request.member.MemberReq;
import com.mtlaa.mychat.chat.domain.vo.response.ChatMemberResp;
import com.mtlaa.mychat.chat.domain.vo.response.ChatRoomResp;
import com.mtlaa.mychat.chat.service.ChatService;
import com.mtlaa.mychat.chat.service.ContactService;
import com.mtlaa.mychat.chat.service.RoomService;
import com.mtlaa.mychat.chat.service.cache.*;
import com.mtlaa.mychat.chat.service.strategy.msg.AbstractMsgHandler;
import com.mtlaa.mychat.chat.service.strategy.msg.MsgHandlerFactory;
import com.mtlaa.mychat.common.domain.vo.request.CursorPageBaseReq;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;
import com.mtlaa.mychat.common.exception.BusinessException;
import com.mtlaa.mychat.common.utils.CursorUtils;
import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.service.cache.UserInfoCache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Create 2023/12/30 15:52
 */
@Service
public class ContactServiceImpl implements ContactService {
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private HotRoomCache hotRoomCache;
    @Autowired
    private RoomFriendCache roomFriendCache;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private RoomService roomService;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private UserDao userDao;


    @Override
    public CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid) {
        // 这里的游标是 active_time
        CursorPageBaseResp<Long> ids;
        if (Objects.isNull(uid)){
            // 当前为游客，只能看到热点群聊（全员群）
            CursorPageBaseResp<Pair<Long, Double>> roomCursorPage = hotRoomCache.getRoomCursorPage(request);
            List<Long> roomIds = roomCursorPage.getList().stream().map(Pair::getKey).collect(Collectors.toList());
            ids = CursorPageBaseResp.init(roomCursorPage, roomIds);
        } else {
            Double end = getCursorOrNull(request.getCursor());
            Double start = null;
            // 用户已经登录，查询uid对应的会话列表
            CursorPageBaseResp<Contact> contactPage = contactDao.getPage(request, uid);
            List<Long> baseRoomIds = contactPage.getList().stream().map(Contact::getRoomId).collect(Collectors.toList());
            if (!contactPage.getIsLast()){
                start = getCursorOrNull(contactPage.getCursor());
            }
            // start -> end 是一个时间范围，我们还需要额外查出在这个时间范围内的热点会话，进行聚合
            Set<ZSetOperations.TypedTuple<String>> hotRoomRange = hotRoomCache.getRoomRange(start, end);
            List<Long> hotRoomIds = hotRoomRange.stream().map(ZSetOperations.TypedTuple::getValue)
                            .filter(Objects::nonNull).map(Long::parseLong).collect(Collectors.toList());

            // 先直接合并，后面组装完再排序
            baseRoomIds.addAll(hotRoomIds);
            ids = CursorPageBaseResp.init(contactPage, baseRoomIds);
        }
        // 最后组装会话信息（名称，头像，未读数等），并排序
        List<ChatRoomResp> respList = buildContactResp(uid, ids.getList());
        return CursorPageBaseResp.init(ids, respList);
    }

    @Override
    public ChatRoomResp getContactDetail(Long uid, long roomId) {
        Room room = roomCache.get(roomId);
        if (room == null){
            throw new BusinessException("房间不存在");
        }
        return buildContactResp(uid, Collections.singletonList(roomId)).get(0);
    }

    @Override
    public ChatRoomResp getContactDetailByFriend(Long uid, Long uid1) {
        RoomFriend friendRoom = roomService.getFriendRoom(uid, uid1);
        if (friendRoom == null){
            throw new BusinessException("还不是好友");
        }
        return buildContactResp(uid, Collections.singletonList(friendRoom.getRoomId())).get(0);
    }

    @Override
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request) {
        Room room = roomCache.get(request.getRoomId());
        if (room == null){
            throw new BusinessException("房间号有误");
        }
        List<Long> memberUidList;
        if (room.isHotRoom()) {// 全员群展示所有用户
            memberUidList = userDao.getAllUid();
        } else {// 只展示房间内的群成员
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            memberUidList = groupMemberDao.getMemberUidList(roomGroup.getId());
        }
        Map<Long, User> userMap = userInfoCache.getBatch(memberUidList);
        List<ChatMemberResp> respList = userMap.values().stream().map(user -> {
            ChatMemberResp chatMemberResp = new ChatMemberResp();
            chatMemberResp.setUid(user.getId());
            chatMemberResp.setActiveStatus(user.getActiveStatus());
            return chatMemberResp;

        }).collect(Collectors.toList());
        return new CursorPageBaseResp<>(null, true, respList);
    }

    private List<ChatRoomResp> buildContactResp(Long uid, List<Long> roomIds) {
        // 获取房间信息：名称、头像、是否热点
        Map<Long, RoomBaseInfo> roomBaseInfoMap = getRoomBaseInfoMap(uid, roomIds);
        // 查询出房间的最后一条消息
        List<Long> msgIds = roomBaseInfoMap.values().stream().map(RoomBaseInfo::getLastMsgId).collect(Collectors.toList());
        List<Message> messages = CollectionUtil.isEmpty(msgIds) ? new ArrayList<>() : messageDao.listByIds(msgIds);
        //  msgId -> Message
        Map<Long, Message> messageMap = messages.stream().collect(Collectors.toMap(Message::getId, Function.identity()));
        //  uid -> User
        Map<Long, User> userMap = userInfoCache.getBatch(
                messageMap.values().stream().map(Message::getFromUid).collect(Collectors.toList()));
        // 消息未读数
        Map<Long, Integer> unReadCountMap = getUnreadCountMap(roomIds, uid);
        return roomBaseInfoMap.values().stream().map(roomBaseInfo -> {
            ChatRoomResp chatRoomResp = new ChatRoomResp();
            BeanUtils.copyProperties(roomBaseInfo, chatRoomResp);
            chatRoomResp.setHot_Flag(roomBaseInfo.getHotFlag());
            chatRoomResp.setUnreadCount(unReadCountMap.getOrDefault(roomBaseInfo.getRoomId(), 0));
            Message message = messageMap.get(roomBaseInfo.getLastMsgId());
            if (Objects.nonNull(message)) {
                AbstractMsgHandler<?> abstractMsgHandler = MsgHandlerFactory.getStrategyNoNull(message.getType());
                chatRoomResp.setText(userMap.get(message.getFromUid()).getName() + ":" +
                        abstractMsgHandler.showContactMsg(message));
            }
            return chatRoomResp;
        }).collect(Collectors.toList());
    }

    private Map<Long, Integer> getUnreadCountMap(List<Long> roomIds, Long uid) {
        if (CollectionUtil.isEmpty(roomIds)){
            return new HashMap<>();
        }
        List<Contact> contacts = contactDao.listByRoomIds(roomIds, uid);
        return contacts.stream().collect(Collectors.toMap(Contact::getRoomId, contact -> {
            return messageDao.unreadCount(contact.getReadTime(), contact.getRoomId());
        }));
    }


    private Map<Long, RoomBaseInfo> getRoomBaseInfoMap(Long uid, List<Long> roomIds) {
        // 获取房间信息
        Map<Long, Room> roomMap = roomCache.getBatch(roomIds);
        // 按单聊群聊分开
        Map<Integer, List<Long>> collect = roomMap.values().stream().collect(
                Collectors.groupingBy(Room::getType, Collectors.mapping(Room::getId, Collectors.toList())));
        // 获取单聊，单聊会话的名称和头像来自好友
        List<Long> friendRoomIds = collect.get(RoomTypeEnum.FRIEND.getType());
        Map<Long, User> friendMap = getFriendUserMap(uid, friendRoomIds);  // roomId -> User
        // 获取群聊
        List<Long> groupRoomIds = collect.get(RoomTypeEnum.GROUP.getType());
        Map<Long, RoomGroup> groupMap = roomGroupCache.getBatch(groupRoomIds);
        return roomMap.values().stream().collect(Collectors.toMap(Room::getId, room -> {
            RoomBaseInfo roomBaseInfo = new RoomBaseInfo();
            roomBaseInfo.setRoomId(room.getId());
            roomBaseInfo.setType(room.getType());
            roomBaseInfo.setHotFlag(room.getHotFlag());
            roomBaseInfo.setActiveTime(room.getActiveTime());
            roomBaseInfo.setLastMsgId(room.getLastMsgId());
            if (RoomTypeEnum.of(room.getType()) == RoomTypeEnum.FRIEND) {
                roomBaseInfo.setName(friendMap.get(room.getId()).getName());
                roomBaseInfo.setAvatar(friendMap.get(room.getId()).getAvatar());
            } else {
                roomBaseInfo.setName(groupMap.get(room.getId()).getName());
                roomBaseInfo.setAvatar(groupMap.get(room.getId()).getAvatar());
            }
            return roomBaseInfo;
        }));
    }

    private Map<Long, User> getFriendUserMap(Long uid, List<Long> friendRoomIds) {
        if (CollectionUtil.isEmpty(friendRoomIds)){
            return new HashMap<>();
        }
        // roomId -> RoomFriend
        Map<Long, RoomFriend> batch = roomFriendCache.getBatch(friendRoomIds);
        List<Long> uids = batch.values().stream().map(roomFriend -> {
            return Objects.equals(roomFriend.getUid1(), uid) ? roomFriend.getUid2() : roomFriend.getUid1();
        }).collect(Collectors.toList());
        // uid -> User
        Map<Long, User> userMap = userInfoCache.getBatch(uids);
        // 需要返回 roomId -> User
        return batch.values().stream().collect(Collectors.toMap(RoomFriend::getRoomId, roomFriend -> {
            Long friendUid = roomFriend.getUid1();
            if (friendUid.equals(uid)) {
                friendUid = roomFriend.getUid2();
            }
            return userMap.get(friendUid);
        }));
    }


    private Double getCursorOrNull(String cursor) {
        return Optional.ofNullable(cursor).map(Double::parseDouble).orElse(null);
    }
}

package com.mtlaa.mtchat.chat.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;

import com.mtlaa.mtchat.cache.chat.*;
import com.mtlaa.mtchat.cache.user.UserInfoCache;
import com.mtlaa.mtchat.chat.dao.ContactDao;
import com.mtlaa.mtchat.chat.dao.GroupMemberDao;
import com.mtlaa.mtchat.chat.dao.MessageDao;
import com.mtlaa.mtchat.strategy.msghandler.AbstractMsgHandler;
import com.mtlaa.mtchat.strategy.msghandler.MsgHandlerFactory;
import com.mtlaa.mtchat.chat.service.ContactService;
import com.mtlaa.mtchat.chat.service.RoomService;
import com.mtlaa.mtchat.domain.chat.dto.RoomBaseInfo;
import com.mtlaa.mtchat.domain.chat.entity.Message;
import com.mtlaa.mtchat.domain.chat.entity.Room;
import com.mtlaa.mtchat.domain.chat.entity.RoomFriend;
import com.mtlaa.mtchat.domain.chat.entity.RoomGroup;
import com.mtlaa.mtchat.domain.chat.enums.HotFlagEnum;
import com.mtlaa.mtchat.domain.chat.enums.RoomTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberReq;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMemberResp;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatRoomResp;
import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.user.dao.UserDao;
import com.mtlaa.mtchat.utils.DateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import com.mtlaa.mtchat.domain.chat.entity.Contact;

import java.time.ZoneOffset;
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
    @Autowired
    private MsgCache msgCache;


    @Override
    public CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid) {
        // 这里的游标是 active_time
        CursorPageBaseResp<Long> ids;
        Map<Long, Date> activeTimeMap = new HashMap<>();  // roomId, activeTime
        Map<Long, Long> lastMsgIdMap = new HashMap<>();
        if (Objects.isNull(uid)){
            // 当前为游客，只能看到热点群聊（全员群）
            CursorPageBaseResp<Pair<Long, Double>> roomCursorPage = hotRoomCache.getRoomCursorPage(request);
            //                     roomId, activeTime
            List<Long> roomIds = roomCursorPage.getList().stream().map(Pair::getKey).collect(Collectors.toList());

            ids = CursorPageBaseResp.init(roomCursorPage, roomIds);
        } else {
            Double end = getCursorOrNull(request.getCursor()); // 本次查询的游标
            Double start = null;  // 查询之后的游标
            // 用户已经登录，查询uid对应的会话列表
            CursorPageBaseResp<Contact> contactPage = contactDao.getPage(request, uid);

            List<Long> baseRoomIds = contactPage.getList().stream().map(contact -> {
                activeTimeMap.put(contact.getRoomId(), Date.from(contact.getActiveTime().toInstant(ZoneOffset.of("+8"))));
                lastMsgIdMap.put(contact.getRoomId(), contact.getLastMsgId());
                return contact.getRoomId();
            }).collect(Collectors.toList());

            if (!contactPage.getIsLast()){
                start = getCursorOrNull(contactPage.getCursor());
            }
            // start -> end 是一个时间范围，我们还需要额外查出在这个时间范围内的热点会话，进行聚合
            Set<ZSetOperations.TypedTuple<String>> hotRoomRange = hotRoomCache.getRoomRange(start, end);
            List<Long> hotRoomIds = hotRoomRange.stream().map(ZSetOperations.TypedTuple::getValue)
                            .filter(Objects::nonNull).map(Long::parseLong).collect(Collectors.toList());

            // 先直接合并，后面组装完再排序
            baseRoomIds.addAll(hotRoomIds);
            ids = CursorPageBaseResp.init(contactPage, baseRoomIds); // 这里仅仅是这一次翻页查到的一页会话的【房间id】
            // 由于需要返回的会话列表需要会话的名称头像，但是 contact 表里没有，所以这里先查出会话对应的 roomId，然后根据 roomId查
        }
        // 在本方法内查询出的 roomId对应的activeTime和lastMsgId可以传递给 buildContactResp方法，避免重复查询
        // 最后组装会话信息（名称，头像，未读数等），并排序
        List<ChatRoomResp> respList = buildContactResp(uid, ids.getList(), activeTimeMap, lastMsgIdMap);
        return CursorPageBaseResp.init(ids, respList);
    }

    @Override
    public ChatRoomResp getContactDetail(Long uid, long roomId) {
        Room room = roomCache.get(roomId);
        if (room == null){
            throw new BusinessException("房间不存在");
        }
        Contact contact = contactDao.getByRoomIdAndUid(roomId, uid);
        return buildContactResp(uid, Collections.singletonList(roomId),
                Collections.singletonMap(roomId, Date.from(contact.getActiveTime().toInstant(ZoneOffset.of("+8")))),
                Collections.singletonMap(roomId, contact.getLastMsgId())).get(0);
    }

    @Override
    public ChatRoomResp getContactDetailByFriend(Long uid, Long uid1) {
        RoomFriend friendRoom = roomService.getFriendRoom(uid, uid1);
        if (friendRoom == null){
            throw new BusinessException("还不是好友");
        }
        Contact contact = contactDao.getByRoomIdAndUid(friendRoom.getRoomId(), uid);
        return buildContactResp(uid, Collections.singletonList(friendRoom.getRoomId()),
                Collections.singletonMap(friendRoom.getRoomId(), Date.from(contact.getActiveTime().toInstant(ZoneOffset.of("+8")))),
                Collections.singletonMap(friendRoom.getRoomId(), contact.getLastMsgId())).get(0);
    }

    /**
     * FIXME 直接返回了所有群成员，这里应该进行游标翻页
     * @param request 指定roomId
     * @return 该群聊的所有群成员
     */
    @Override
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request) {
        Room room = roomCache.get(request.getRoomId());
        if (room == null){
            throw new BusinessException("房间号有误");
        }
        List<Long> memberUidList;
        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
        if (room.isHotRoom()) {// 全员群展示所有用户
            memberUidList = userDao.getAllUid();
        } else {// 只展示房间内的群成员
            memberUidList = groupMemberDao.getMemberUidList(roomGroup.getId());
        }
        Map<Long, User> userMap = userInfoCache.getBatch(memberUidList);
        List<ChatMemberResp> respList = userMap.values().stream().map(user -> {
            ChatMemberResp chatMemberResp = new ChatMemberResp();
            chatMemberResp.setUid(user.getId());
            chatMemberResp.setActiveStatus(user.getActiveStatus());
            chatMemberResp.setLastOptTime(DateUtil.LocalDateTime2Date(user.getLastOptTime()));
            if (!room.isHotRoom())
                chatMemberResp.setRoleId(groupMemberDao.getByUidAndGroupId(user.getId(), roomGroup.getId()).getRole());
            return chatMemberResp;
        }).collect(Collectors.toList());
        return new CursorPageBaseResp<>(null, true, respList);
    }

    /**
     * 根据 uid 以及 roomId 查询会话信息
     * @param uid 用户id，主要用于配合房间id查询 contact 表获取 未读消息数
     * @param roomIds 房间id，主要用于查询会话的名称和头像
     * @return 会话展示
     */
    private List<ChatRoomResp> buildContactResp(Long uid, List<Long> roomIds, Map<Long, Date> activeTimeMap,
                                                Map<Long, Long> lastMsgIdMap) {
        // 获取房间信息：名称、头像、是否热点
        Map<Long, RoomBaseInfo> roomBaseInfoMap = getRoomBaseInfoMap(uid, roomIds, activeTimeMap, lastMsgIdMap);
        // 查询出房间的最后一条消息
        List<Long> msgIds = roomBaseInfoMap.values().stream().map(RoomBaseInfo::getLastMsgId).collect(Collectors.toList());
        //  msgId -> Message
        Map<Long, Message> messageMap = CollectionUtil.isEmpty(msgIds) ? new HashMap<>() : msgCache.getBatch(msgIds);
        //  uid -> User
        Map<Long, User> userMap = userInfoCache.getBatch(
                messageMap.values().stream().map(Message::getFromUid).collect(Collectors.toList()));
        // 消息未读数
        Map<Long, Integer> unReadCountMap = getUnreadCountMap(roomIds, uid);
        return roomBaseInfoMap.values().stream().map(roomBaseInfo -> {
            ChatRoomResp chatRoomResp = new ChatRoomResp();
            // FIXED 只有热点群聊的activeTime!=null
            BeanUtils.copyProperties(roomBaseInfo, chatRoomResp);
            chatRoomResp.setHot_Flag(roomBaseInfo.getHotFlag());
            chatRoomResp.setUnreadCount(unReadCountMap.getOrDefault(roomBaseInfo.getRoomId(), 0));
            Message message = messageMap.get(roomBaseInfo.getLastMsgId());
            if (Objects.nonNull(message)) {
                AbstractMsgHandler<?> abstractMsgHandler = MsgHandlerFactory.getStrategyNoNull(message.getType());
                chatRoomResp.setText(userMap.get(message.getFromUid()).getName() + ": " +
                        abstractMsgHandler.showContactMsg(message));
            }
            return chatRoomResp;
        }).collect(Collectors.toList());
    }

    /**
     * 获取当前用户在 room 有多少条未读消息
     */
    private Map<Long, Integer> getUnreadCountMap(List<Long> roomIds, Long uid) {
        if (CollectionUtil.isEmpty(roomIds)){
            return new HashMap<>();
        }
        List<Contact> contacts = contactDao.listByRoomIds(roomIds, uid);
        return contacts.stream().collect(Collectors.toMap(Contact::getRoomId, contact -> {
            return messageDao.unreadCount(contact.getReadTime(), contact.getRoomId());
        }));
    }


    /**
     * 根据 roomId 获取房间（会话）的基本信息，如名称、头像。分单聊群聊
     * @param uid 用于单聊时获得对方的uid
     * @param roomIds 获取单聊的RoomFriend、群聊的RoomGroup
     * @return roomId -> 房间信息
     */
    private Map<Long, RoomBaseInfo> getRoomBaseInfoMap(Long uid, List<Long> roomIds, Map<Long, Date> activeTimeMap,
                                                       Map<Long, Long> lastMsgIdMap) {
        // 基于旁路缓存的批量缓存---获取房间信息
        Map<Long, Room> roomMap = roomCache.getBatch(roomIds);
        // 按单聊群聊分开
        Map<Integer, List<Long>> collect = roomMap.values().stream().collect(
                Collectors.groupingBy(Room::getType, Collectors.mapping(Room::getId, Collectors.toList())));
        // 获取单聊，单聊会话的名称和头像来自好友
        List<Long> friendRoomIds = collect.get(RoomTypeEnum.FRIEND.getType());
        Map<Long, User> friendMap = getFriendUserMap(uid, friendRoomIds);  // roomId -> User
        // 获取群聊
        List<Long> groupRoomIds = collect.get(RoomTypeEnum.GROUP.getType());
        Map<Long, RoomGroup> groupMap = roomGroupCache.getBatch(groupRoomIds);  // roomId -> RoomGroup
        return roomMap.values().stream().collect(Collectors.toMap(Room::getId, room -> {
            RoomBaseInfo roomBaseInfo = new RoomBaseInfo();
            roomBaseInfo.setRoomId(room.getId());
            roomBaseInfo.setType(room.getType());
            roomBaseInfo.setHotFlag(room.getHotFlag());
            // room为热点群聊时从 room 表获取
            if (room.getHotFlag().equals(HotFlagEnum.YES.getType())){
                roomBaseInfo.setActiveTime(room.getActiveTime());
                roomBaseInfo.setLastMsgId(room.getLastMsgId());
            } else {
                roomBaseInfo.setActiveTime(activeTimeMap.get(room.getId()));
                roomBaseInfo.setLastMsgId(lastMsgIdMap.get(room.getId()));
            }

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

    /**
     * 单聊的名称和头像就是对方的名称和头像。根据单聊的 roomId 查询对方用户 User
     * @param uid 当前用户
     * @param friendRoomIds type为单聊的roomId
     * @return roomId -> User
     */
    private Map<Long, User> getFriendUserMap(Long uid, List<Long> friendRoomIds) {
        if (CollectionUtil.isEmpty(friendRoomIds)){
            return new HashMap<>();
        }
        // roomId -> RoomFriend
        Map<Long, RoomFriend> batch = roomFriendCache.getBatch(friendRoomIds);
        // RoomFriend -> 对方的uid
        List<Long> uids = batch.values().stream().map(
                roomFriend -> Objects.equals(roomFriend.getUid1(), uid) ?
                        roomFriend.getUid2() : roomFriend.getUid1()).collect(Collectors.toList());
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

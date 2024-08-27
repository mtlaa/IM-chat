package com.mtlaa.mtchat.chat.service.impl;


import com.mtlaa.mtchat.cache.chat.GroupMemberCache;
import com.mtlaa.mtchat.cache.chat.RoomCache;
import com.mtlaa.mtchat.cache.chat.RoomGroupCache;
import com.mtlaa.mtchat.cache.user.UserCache;
import com.mtlaa.mtchat.cache.user.UserInfoCache;
import com.mtlaa.mtchat.chat.dao.GroupMemberDao;
import com.mtlaa.mtchat.chat.dao.RoomDao;
import com.mtlaa.mtchat.chat.dao.RoomFriendDao;
import com.mtlaa.mtchat.chat.dao.RoomGroupDao;
import com.mtlaa.mtchat.chat.service.RoomService;
import com.mtlaa.mtchat.domain.chat.entity.GroupMember;
import com.mtlaa.mtchat.domain.chat.entity.Room;
import com.mtlaa.mtchat.domain.chat.entity.RoomFriend;
import com.mtlaa.mtchat.domain.chat.entity.RoomGroup;
import com.mtlaa.mtchat.domain.chat.enums.GroupRoleAPPEnum;
import com.mtlaa.mtchat.domain.chat.enums.HotFlagEnum;
import com.mtlaa.mtchat.domain.chat.enums.RoomTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberAddReq;
import com.mtlaa.mtchat.domain.chat.vo.request.member.MemberDelReq;
import com.mtlaa.mtchat.domain.chat.vo.response.ChatMemberListResp;
import com.mtlaa.mtchat.domain.chat.vo.response.MemberResp;
import com.mtlaa.mtchat.domain.chat.vo.response.wsMsg.WSBaseResp;
import com.mtlaa.mtchat.domain.common.enums.NormalOrNoEnum;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.enums.RoleEnum;
import com.mtlaa.mtchat.domain.user.enums.UserActiveStatusEnum;
import com.mtlaa.mtchat.domain.websocket.enums.WebSocketResponseTypeEnum;
import com.mtlaa.mtchat.domain.websocket.vo.WSMemberChange;
import com.mtlaa.mtchat.event.GroupMemberAddEvent;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.utils.RoomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Create 2023/12/21 19:50
 */
@Service
public class RoomServiceImpl implements RoomService {
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private PushService pushService;
    @Autowired
    private RoomGroupDao roomGroupDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    /**
     * 创建一个单聊
     * 插入room表、room_friend表
     */
    @Override
    @Transactional
    public RoomFriend createFriendRoom(Long uid, Long uid1) {
        String roomKey = RoomUtils.generateRoomKey(uid, uid1);
        RoomFriend roomFriend = roomFriendDao.getByRoomKey(roomKey);
        if(Objects.nonNull(roomFriend)){
            restoreRoomIfNeed(roomFriend);
        }else{
            Room room = createRoom(RoomTypeEnum.FRIEND);
            roomFriend = RoomFriend.builder()
                    .roomId(room.getId())
                    .roomKey(roomKey)
                    .uid1(uid)
                    .uid2(uid1)
                    .createTime(new Date())
                    .updateTime(new Date())
                    .status(NormalOrNoEnum.NORMAL.getStatus())
                    .build();
            roomFriendDao.save(roomFriend);
        }
        return roomFriend;
    }

    /**
     * 禁用房间，status = not normal
     */
    @Override
    public void disableFriendRoom(Long uid, Long targetUid) {
        String roomKey = RoomUtils.generateRoomKey(uid, targetUid);
        roomFriendDao.disableRoom(roomKey);
    }

    @Override
    public RoomFriend getFriendRoom(Long uid, Long uid1) {
        String roomKey = RoomUtils.generateRoomKey(uid, uid1);
        return roomFriendDao.getByRoomKey(roomKey);
    }

    /**
     * 获取群组详情
     * @param uid uid
     * @param roomId 房间id
     * @return 群组详情，包含当前uid的角色，以及在线人数
     */
    @Override
    public MemberResp getGroupDetail(Long uid, long roomId) {
        RoomGroup roomGroup = roomGroupCache.get(roomId);
        Room room = roomCache.get(roomId);

        Long onlineNum;
        if (room.isHotRoom()){
            onlineNum = userCache.getOnlineNum();
        } else {
            List<Long> memberUidList = groupMemberCache.getMemberUidList(roomId);
            Map<Long, User> userMap = userInfoCache.getBatch(memberUidList);
            onlineNum = userMap.values().stream()
                    .filter(user -> user.getActiveStatus().equals(UserActiveStatusEnum.ONLINE.getStatus()))
                    .count();
        }
        GroupRoleAPPEnum userRole = getGroupRole(uid, roomGroup.getId(), room);

        return MemberResp.builder()
                .onlineNum(onlineNum)
                .avatar(roomGroup.getAvatar())
                .groupName(roomGroup.getName())
                .roomId(roomId)
                .role(userRole.getType())
                .build();
    }

    /**
     * 获取指定房间内群成员的简略信息
     * @param roomId 房间id
     * @return 成员信息列表
     */
    @Override
    public List<ChatMemberListResp> getMemberList(Long roomId) {
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomId);
        Map<Long, User> userMap = userInfoCache.getBatch(memberUidList);

        return userMap.values().stream().map(user -> {
            ChatMemberListResp chatMemberListResp = new ChatMemberListResp();
            chatMemberListResp.setUid(user.getId());
            chatMemberListResp.setName(user.getName());
            chatMemberListResp.setAvatar(user.getAvatar());
            return chatMemberListResp;
        }).collect(Collectors.toList());
    }

    /**
     * 删除群聊中的某个人：删除group_member的记录
     * <p>全员群不可移除成员</p>
     * @param uid 操作者uid
     * @param request 包含：roomId，被删除人uid
     */
    @Override
    @Transactional
    public void delMember(Long uid, MemberDelReq request) {
        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
        Room room = roomCache.get(request.getRoomId());
        if (Objects.isNull(room) || Objects.isNull(roomGroup)){
            throw new BusinessException("房间号有误");
        }
        GroupMember opr = groupMemberDao.getByUidAndGroupId(uid, roomGroup.getId());
        if (Objects.isNull(opr)){
            throw new BusinessException("操作者不在群里");
        }
        // 校验权限
        GroupRoleAPPEnum delUserRole = getGroupRole(request.getUid(), roomGroup.getId(), room);  // 被删除人的角色
        GroupRoleAPPEnum opUserRole = getGroupRole(uid, roomGroup.getId(), room);  // 操作人的角色
        switch (delUserRole){
            case LEADER:
                throw new BusinessException("不可以删除群主");
            case MANAGER:
                if (!opUserRole.equals(GroupRoleAPPEnum.LEADER)){
                    throw new BusinessException("只有群主才可以删除管理员");
                }
                break;
            case MEMBER:
                if (!opUserRole.equals(GroupRoleAPPEnum.LEADER) && !opUserRole.equals(GroupRoleAPPEnum.MANAGER)){
                    throw new BusinessException("没有权限!");
                }
                break;
            case REMOVE:
                throw new BusinessException("该用户已经被删除");
        }
        GroupMember delMember = groupMemberDao.getByUidAndGroupId(request.getUid(), roomGroup.getId());
        if (Objects.isNull(delMember)){
            throw new BusinessException("该用户已经被删除");
        }
        groupMemberDao.removeById(delMember.getId());

        // 发送移除消息给其他群成员
        List<Long> memberUidList = groupMemberCache.getMemberUidList(request.getRoomId());
        WSMemberChange wsMemberChange = WSMemberChange.builder()
                .uid(delMember.getUid())
                .changeType(WSMemberChange.CHANGE_TYPE_REMOVE)
                .roomId(request.getRoomId())
                .build();
        pushService.sendPushMsg(
                new WSBaseResp<>(WebSocketResponseTypeEnum.MEMBER_CHANGE.getType(), wsMemberChange), memberUidList);
        groupMemberCache.evictMemberUidList(request.getRoomId());
    }

    /**
     * 创建群组，并且添加群成员。最后推送一个消息，触发被添加用户的对话
     * @param uid 创建者：群主
     * @param uidList 邀请的人uid列表
     * @return roomId 房间id
     */
    @Override
    @Transactional
    public Long addGroup(Long uid, List<Long> uidList) {
        RoomGroup roomGroup = createGroupRoom(uid);
        // 添加群成员，删除缓存
        List<GroupMember> groupMemberList = addMemberToGroup(uidList, roomGroup);
        // 发出事件，异步触发被添加人员的对话（会话contact的创建会推迟到推送消息更新每个用户的会话表时）
        applicationEventPublisher.publishEvent(new GroupMemberAddEvent(this, groupMemberList, roomGroup, uid));
        return roomGroup.getRoomId();
    }

    /**
     * 添加群成员
     * @param uid 邀请人
     * @param request 所在roomId以及被添加的uid
     */
    @Override
    @Transactional
    public void addMember(Long uid, MemberAddReq request) {
        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
        if (Objects.isNull(roomGroup)){
            throw new BusinessException("群聊不存在");
        }
        Set<Long> memberSet = new HashSet<>(groupMemberCache.getMemberUidList(request.getRoomId()));
        List<Long> addMemberUidList = request.getUidList().stream()
                .filter(curUid -> !memberSet.contains(curUid)).collect(Collectors.toList());

        List<GroupMember> groupMemberList = addMemberToGroup(addMemberUidList, roomGroup);

        applicationEventPublisher.publishEvent(new GroupMemberAddEvent(this, groupMemberList, roomGroup, uid));
    }

    /**
     * 向指定的群聊中添加群成员，然后清除该群聊的成员缓存
     * @param addMemberUidList 添加的成员
     * @param roomGroup 群聊
     * @return 被添加的群成员
     */
    private List<GroupMember> addMemberToGroup(List<Long> addMemberUidList, RoomGroup roomGroup) {
        List<GroupMember> groupMemberList = addMemberUidList.stream().map(curUid -> GroupMember.builder()
                .uid(curUid)
                .groupId(roomGroup.getId())
                .role(GroupRoleAPPEnum.MEMBER.getType())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build()).collect(Collectors.toList());
        groupMemberDao.saveBatch(groupMemberList);
        groupMemberCache.evictMemberUidList(roomGroup.getRoomId());
        return groupMemberList;
    }

    /**
     * 创建群聊：包括创建房间、RoomGroup、添加群主
     * <p>群聊默认名为：创建人name + 的群聊</p>
     * <p>每个人只能创建一个群聊</p>
     * @param uid 创建人
     * @return 创建的群聊
     */
    private RoomGroup createGroupRoom(Long uid) {
        GroupMember groupMember = groupMemberDao.getByUidAndRole(uid, GroupRoleAPPEnum.LEADER);
        if (Objects.nonNull(groupMember)){
            throw new BusinessException("每个人最多创建一个群聊");
        }
        Room room = createRoom(RoomTypeEnum.GROUP);
        User user = userInfoCache.get(uid);

        RoomGroup roomGroup = RoomGroup.builder()
                .roomId(room.getId())
                .name(user.getName() + "的群聊")
                .avatar(user.getAvatar())  // TODO 头像学微信
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        roomGroupDao.save(roomGroup);

        groupMember = GroupMember.builder()
                .groupId(roomGroup.getId())
                .uid(uid)
                .role(GroupRoleAPPEnum.LEADER.getType())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        groupMemberDao.save(groupMember);
        return roomGroup;
    }

    /**
     * 获取指定用户在指定群聊里的 角色
     * @param uid 指定用户
     * @param groupId 指定群组
     * @param room 指定房间
     * @return 角色类型
     */
    private GroupRoleAPPEnum getGroupRole(Long uid, Long groupId, Room room) {
        GroupMember member = Objects.isNull(uid) ? null : groupMemberDao.getByUidAndGroupId(uid, groupId);
        if (Objects.nonNull(member)) {
            return GroupRoleAPPEnum.of(member.getRole());
        } else if (room.isHotRoom()) {
            return GroupRoleAPPEnum.MEMBER;  // 热点群聊没有GroupMember表
        } else {
            return GroupRoleAPPEnum.REMOVE;
        }
    }

    /**
     * 创建一个非热点房间
     * @param roomTypeEnum 房间的类型：单聊、群聊
     * @return 创建的room
     */
    private Room createRoom(RoomTypeEnum roomTypeEnum) {
        Room room = Room.builder()
                .hotFlag(HotFlagEnum.NOT.getType())
                .type(roomTypeEnum.getType())
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        roomDao.save(room);
        return room;
    }

    /**
     * 重新添加好友--判断是否要恢复房间
     */
    private void restoreRoomIfNeed(RoomFriend roomFriend) {
        if(roomFriend.getStatus().equals(NormalOrNoEnum.NOT_NORMAL.getStatus())){
            // 房间被禁用，需要恢复
            roomFriendDao.restoreRoom(roomFriend.getId());
        }
    }
}

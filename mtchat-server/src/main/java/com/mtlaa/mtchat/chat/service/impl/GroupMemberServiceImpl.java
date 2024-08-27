package com.mtlaa.mtchat.chat.service.impl;

import com.mtlaa.mtchat.cache.chat.GroupMemberCache;
import com.mtlaa.mtchat.cache.chat.MsgCache;
import com.mtlaa.mtchat.cache.chat.RoomCache;
import com.mtlaa.mtchat.cache.chat.RoomGroupCache;
import com.mtlaa.mtchat.chat.dao.ContactDao;
import com.mtlaa.mtchat.chat.dao.GroupMemberDao;
import com.mtlaa.mtchat.chat.dao.MessageDao;
import com.mtlaa.mtchat.chat.service.GroupMemberService;
import com.mtlaa.mtchat.domain.chat.entity.GroupMember;
import com.mtlaa.mtchat.domain.chat.entity.Room;
import com.mtlaa.mtchat.domain.chat.entity.RoomGroup;
import com.mtlaa.mtchat.domain.chat.enums.GroupRoleAPPEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.admin.AdminAddReq;
import com.mtlaa.mtchat.domain.chat.vo.request.admin.AdminRevokeReq;
import com.mtlaa.mtchat.domain.chat.vo.response.wsMsg.WSBaseResp;
import com.mtlaa.mtchat.domain.websocket.enums.WebSocketResponseTypeEnum;
import com.mtlaa.mtchat.domain.websocket.vo.WSMemberChange;
import com.mtlaa.mtchat.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class GroupMemberServiceImpl implements GroupMemberService {
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;
    /**
     * 退出群聊：删除group_member、contact
     * <p>如果是群主退出，则还需要删除房间以及房间的消息</p>
     * <p>全员群不可以退出</p>
     * @param uid 操作者uid
     * @param roomId 房间号
     */
    @Override
    @Transactional
    public void exitGroup(Long uid, Long roomId) {
        Room room = roomCache.get(roomId);
        RoomGroup roomGroup = roomGroupCache.get(roomId);
        if (Objects.isNull(room) || Objects.isNull(roomGroup)){
            throw new BusinessException("房间不存在");
        }
        if (room.isHotRoom()){
            throw new BusinessException("全员群不可以退出");
        }

        GroupMember self = groupMemberDao.getByUidAndGroupId(uid, roomGroup.getId());
        if (Objects.isNull(self)){
            throw new BusinessException("用户不在群里");
        }
        // 是群主
        if (self.getRole().equals(GroupRoleAPPEnum.LEADER.getType())){
            // 删除房间
            roomCache.remove(roomId);
            // 删除群聊
            roomGroupCache.remove(roomId);
            // 删除该房间的所有会话
            contactDao.removeByRoomId(roomId, Collections.emptyList());
            // 删除该房间的所有成员
            groupMemberDao.removeByGroupId(roomGroup.getId(), Collections.emptyList());
            // 逻辑删除该房间的所有消息
            messageDao.removeByRoomIdLogic(roomId);
        } else {
            contactDao.removeByRoomId(roomId, Collections.singletonList(uid));
            groupMemberDao.removeByGroupId(roomGroup.getId(), Collections.singletonList(uid));
            List<Long> memberUidList = groupMemberCache.getMemberUidList(roomId);
            WSMemberChange wsMemberChange = WSMemberChange.builder()
                    .uid(uid)
                    .changeType(WSMemberChange.CHANGE_TYPE_REMOVE)
                    .roomId(roomId)
                    .build();
            pushService.sendPushMsg(
                    new WSBaseResp<>(WebSocketResponseTypeEnum.MEMBER_CHANGE.getType(), wsMemberChange), memberUidList);
        }
        groupMemberCache.evictMemberUidList(roomId);
    }

    /**
     * 群聊添加管理员
     * @param uid 操作人。必须是群主
     * @param request 所在roomId，以及被添加的uid列表
     */
    @Override
    public void addAdmin(Long uid, AdminAddReq request) {
        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
        if (Objects.isNull(roomGroup)){
            throw new BusinessException("房间不存在");
        }
        GroupMember opr = groupMemberDao.getByUidAndGroupId(uid, roomGroup.getId());
        if (Objects.isNull(opr) || !opr.getRole().equals(GroupRoleAPPEnum.LEADER.getType())){
            throw new BusinessException("您没有权限");
        }
        List<GroupMember> groupMemberList = groupMemberDao.listByUidListAndGroupId(request.getUidList(), roomGroup.getId());
        if (groupMemberList.size() != request.getUidList().size()){
            throw new BusinessException("某用户不在当前群聊中");
        }
        groupMemberList.forEach(groupMember -> {
            if (!groupMember.getRole().equals(GroupRoleAPPEnum.MEMBER.getType())){
                throw new BusinessException("'" + groupMember.getUid() + "'已经是管理员");
            }
        });
        groupMemberDao.addAdminBatch(roomGroup.getId(), request.getUidList());
    }

    /**
     * 撤销管理员
     * @param uid 操作人
     * @param request 所在roomId，以及指定的uid列表
     */
    @Override
    public void revokeAdmin(Long uid, AdminRevokeReq request) {
        RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
        if (Objects.isNull(roomGroup)){
            throw new BusinessException("房间不存在");
        }
        GroupMember opr = groupMemberDao.getByUidAndGroupId(uid, roomGroup.getId());
        if (Objects.isNull(opr) || !opr.getRole().equals(GroupRoleAPPEnum.LEADER.getType())){
            throw new BusinessException("您没有权限");
        }
        List<GroupMember> groupMemberList = groupMemberDao.listByUidListAndGroupId(request.getUidList(), roomGroup.getId());
        if (groupMemberList.size() != request.getUidList().size()){
            throw new BusinessException("某用户不在当前群聊中");
        }
        groupMemberList.forEach(groupMember -> {
            if (!groupMember.getRole().equals(GroupRoleAPPEnum.MANAGER.getType())){
                throw new BusinessException("'" + groupMember.getUid() + "'不是管理员");
            }
        });
        groupMemberDao.revokeAdminBatch(roomGroup.getId(), request.getUidList());
    }
}

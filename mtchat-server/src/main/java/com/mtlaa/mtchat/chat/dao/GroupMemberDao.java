package com.mtlaa.mtchat.chat.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mtlaa.mtchat.chat.mapper.GroupMemberMapper;
import com.mtlaa.mtchat.domain.chat.entity.GroupMember;
import com.mtlaa.mtchat.domain.chat.enums.GroupRoleAPPEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 群成员表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-25
 */
@Service
public class GroupMemberDao extends ServiceImpl<GroupMemberMapper, GroupMember> {

    public GroupMember getByUidAndGroupId(Long uid, Long groupId) {
        return lambdaQuery().eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUid, uid)
                .one();
    }

    public List<Long> getMemberByGroupId(Long groupId) {
        List<GroupMember> list = lambdaQuery().eq(GroupMember::getGroupId, groupId).select(GroupMember::getUid).list();
        return list.stream().map(GroupMember::getUid).collect(Collectors.toList());
    }

    public List<Long> getMemberUidList(Long groupId) {
        return lambdaQuery().eq(GroupMember::getGroupId, groupId)
                .select(GroupMember::getUid)
                .list().stream().map(GroupMember::getUid).collect(Collectors.toList());
    }

    /**
     * 删除指定群聊的指定群成员
     * @param groupId 指定群聊
     * @param uidList 指定群成员列表。为空则删除所有
     */
    public void removeByGroupId(Long groupId, List<Object> uidList) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId);
        if (!CollectionUtil.isEmpty(uidList)){
            wrapper.in(GroupMember::getUid, uidList);
        }
        remove(wrapper);
    }

    /**
     * 查询指定uid、指定角色的GroupMember
     * @param uid uid
     * @param groupRoleAPPEnum 指定角色
     * @return GroupMember
     */
    public GroupMember getByUidAndRole(Long uid, GroupRoleAPPEnum groupRoleAPPEnum) {
        return lambdaQuery().eq(GroupMember::getUid, uid)
                .eq(GroupMember::getRole, groupRoleAPPEnum.getType())
                .one();
    }

    public List<GroupMember> listByUidListAndGroupId(List<Long> uidList, Long groupId) {
        return lambdaQuery().eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getUid, uidList)
                .list();
    }

    public void addAdminBatch(Long groupId, List<Long> uidList) {
        lambdaUpdate().eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getUid, uidList)
                .set(GroupMember::getRole, GroupRoleAPPEnum.MANAGER.getType())
                .update();
    }

    public void revokeAdminBatch(Long groupId, List<Long> uidList) {
        lambdaUpdate().eq(GroupMember::getGroupId, groupId)
                .in(GroupMember::getUid, uidList)
                .set(GroupMember::getRole, GroupRoleAPPEnum.MEMBER.getType())
                .update();
    }
}

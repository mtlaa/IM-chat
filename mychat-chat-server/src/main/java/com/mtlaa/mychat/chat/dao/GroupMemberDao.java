package com.mtlaa.mychat.chat.dao;

import com.mtlaa.mychat.chat.domain.entity.GroupMember;
import com.mtlaa.mychat.chat.mapper.GroupMemberMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}

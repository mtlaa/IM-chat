package com.mtlaa.mychat.user.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mtlaa.mychat.user.domain.entity.UserApply;
import com.mtlaa.mychat.user.domain.enums.ApplyReadStatusEnum;
import com.mtlaa.mychat.user.domain.enums.ApplyStatusEnum;
import com.mtlaa.mychat.user.domain.enums.ApplyTypeEnum;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendApplyResp;
import com.mtlaa.mychat.user.mapper.UserApplyMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户申请表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Service
public class UserApplyDao extends ServiceImpl<UserApplyMapper, UserApply>{

    public UserApply getByUidAndTargetUidWithUnAplly(Long uid, Long targetUid) {
        return lambdaQuery()
                .eq(UserApply::getUid, uid)
                .eq(UserApply::getTargetId, targetUid)
                .eq(UserApply::getStatus, ApplyStatusEnum.WAIT_APPROVAL.getCode())
                .eq(UserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .one();
    }

    public Integer getUnreadCount(Long uid){
        return lambdaQuery()
                .eq(UserApply::getUid, uid)
                .eq(UserApply::getStatus, ApplyStatusEnum.WAIT_APPROVAL)
                .eq(UserApply::getReadStatus, ApplyReadStatusEnum.UNREAD)
                .eq(UserApply::getType, ApplyTypeEnum.ADD_FRIEND)
                .count();
    }

    public IPage<UserApply> friendApplyPage(Long uid, Page page) {
        return lambdaQuery()
                .eq(UserApply::getTargetId, uid)
                .eq(UserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .orderByDesc(UserApply::getCreateTime)
                .page(page);
    }

    public void readApples(Long uid, List<Long> applyIds) {
        lambdaUpdate()
                .set(UserApply::getReadStatus, ApplyReadStatusEnum.READ.getCode())
                .eq(UserApply::getTargetId, uid)
                .in(UserApply::getId, applyIds)
                .update();
    }

    public void agree(Long applyId) {
        lambdaUpdate()
                .set(UserApply::getStatus, ApplyStatusEnum.AGREE.getCode())
                .eq(UserApply::getId, applyId)
                .update();
    }
}

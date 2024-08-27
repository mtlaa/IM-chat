package com.mtlaa.mtchat.user.dao;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.domain.user.entity.UserFriend;
import com.mtlaa.mtchat.user.mapper.UserFriendMapper;
import com.mtlaa.mtchat.utils.cursor.CursorUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 用户联系人表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Service
public class UserFriendDao extends ServiceImpl<UserFriendMapper, UserFriend> {

    public CursorPageBaseResp<UserFriend> getFriendPage(Long uid, CursorPageBaseReq cursorPageBaseReq) {
//        LambdaQueryWrapper<UserFriend> wrapper = new LambdaQueryWrapper<>();
//        // 查询本uid的好友
//        wrapper.eq(UserFriend::getUid, uid);
//        // 游标是否存在，不存在就不设游标条件，存在就查询大于游标的
//        if(StrUtil.isNotBlank(cursorPageBaseReq.getCursor())){
//            wrapper.gt(UserFriend::getId, Long.parseLong(cursorPageBaseReq.getCursor()));
//        }
//        // 按游标排序
//        wrapper.orderByAsc(UserFriend::getId);
//        // 查询出一页
//        Page<UserFriend> userFriends = this.page(cursorPageBaseReq.plusPage(), wrapper);
//        // 构造返回
//        String cursor = Optional.ofNullable(CollectionUtil.getLast(userFriends.getRecords()))
//                .map(UserFriend::getId)
//                .map(Object::toString)
//                .orElse(null);
//
//        return new CursorPageBaseResp<UserFriend>(cursor,
//                userFriends.getRecords().size() != cursorPageBaseReq.getPageSize(),
//                userFriends.getRecords());

        return CursorUtils.getCursorPageByMysql(this, cursorPageBaseReq,
                wrapper -> wrapper.eq(UserFriend::getUid, uid), UserFriend::getId);
    }

    public UserFriend getByUidAndFriendUid(Long uid, Long targetUid) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, targetUid)
                .one();
    }

    public List<UserFriend> getUserFriend(Long uid, Long targetUid) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, targetUid)
                .or()
                .eq(UserFriend::getFriendUid, uid)
                .eq(UserFriend::getUid, targetUid)
                .list();
    }

    public List<UserFriend> getBatchUserFriend(Long uid, List<Long> uidList) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getDeleteStatus, 0)
                .in(UserFriend::getFriendUid, uidList)
                .list();
    }
}

package com.mtlaa.mychat.user.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mtlaa.mychat.common.domain.vo.request.CursorPageBaseReq;
import com.mtlaa.mychat.common.domain.vo.request.PageBaseReq;
import com.mtlaa.mychat.common.domain.vo.response.CursorPageBaseResp;
import com.mtlaa.mychat.common.domain.vo.response.PageBaseResp;
import com.mtlaa.mychat.common.event.UserApplyEvent;
import com.mtlaa.mychat.common.exception.BusinessException;
import com.mtlaa.mychat.user.dao.UserApplyDao;
import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.dao.UserFriendDao;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.domain.entity.UserApply;
import com.mtlaa.mychat.user.domain.entity.UserFriend;
import com.mtlaa.mychat.user.domain.enums.ApplyReadStatusEnum;
import com.mtlaa.mychat.user.domain.enums.ApplyStatusEnum;
import com.mtlaa.mychat.user.domain.enums.ApplyTypeEnum;
import com.mtlaa.mychat.user.domain.vo.request.friend.FriendApplyReq;
import com.mtlaa.mychat.user.domain.vo.request.friend.FriendApproveReq;
import com.mtlaa.mychat.user.domain.vo.request.friend.FriendCheckReq;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendApplyResp;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendCheckResp;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendResp;
import com.mtlaa.mychat.user.domain.vo.response.friend.FriendUnreadResp;
import com.mtlaa.mychat.user.service.UserFriendService;
import com.mtlaa.mychat.user.service.adapter.FriendAdapter;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Create 2023/12/21 11:00
 */
@Service
@Slf4j
public class UserFriendServiceImpl implements UserFriendService {
    @Autowired
    private UserFriendDao userFriendDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserApplyDao userApplyDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    @Lazy
    private UserFriendService userFriendService;
    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq cursorPageBaseReq) {
        // 根据游标，获取当前用户的一页联系人
        CursorPageBaseResp<UserFriend> friendPage = userFriendDao.getFriendPage(uid, cursorPageBaseReq);
        // 如果获取的为空，返回空页，在里面设置该页为最后一页
        if (CollectionUtils.isEmpty(friendPage.getList())) {
            return CursorPageBaseResp.empty();
        }
        // 把一页的friendUid拿出来
        List<Long> friendUids = friendPage.getList().stream()
                .map(UserFriend::getFriendUid).collect(Collectors.toList());
        List<User> friends = userDao.getFriendList(friendUids); // 查询出这一页的好友信息
        // 构造List<FriendResp>存入CursorPageBaseResp<FriendResp>，FriendResp中只需要保存uid和是否在线
        List<FriendResp> friendResps = FriendAdapter.buildFriend(friendPage.getList(), friends);
        return CursorPageBaseResp.init(friendPage, friendResps);
    }

    @Override
    public void apply(Long uid, FriendApplyReq friendApplyReq) {
        // 判断是否已经是好友
        UserFriend userFriend = userFriendDao.getByUidAndFriendUid(uid, friendApplyReq.getTargetUid());
        if(userFriend != null){
            throw new BusinessException("已经是好友了");
        }
        // 判断是否已经申请了好友（还没同意）
        UserApply userApply = userApplyDao.getByUidAndTargetUidWithUnAplly(uid, friendApplyReq.getTargetUid());
        if(Objects.nonNull(userApply)){
            log.info("已经申请过好友：uid：{}，targetUid：{}", uid, friendApplyReq.getTargetUid());
            return;
        }
        // 判断该targetId是否申请了自己
        userApply = userApplyDao.getByUidAndTargetUidWithUnAplly(friendApplyReq.getTargetUid(), uid);
        if(Objects.nonNull(userApply)){
            // 可以直接走确认申请的逻辑
            userFriendService.applyApprove(uid, new FriendApproveReq(userApply.getId()));
            return;
        }
        // 申请入库
        userApply = UserApply.builder()
                .uid(uid)
                .targetId(friendApplyReq.getTargetUid())
                .msg(friendApplyReq.getMsg())
                .type(ApplyTypeEnum.ADD_FRIEND.getCode())
                .status(ApplyStatusEnum.WAIT_APPROVAL.getCode())
                .readStatus(ApplyReadStatusEnum.UNREAD.getCode())
                .build();
        userApplyDao.save(userApply);
        // 发出申请好友的事件，用于给被申请的人推送通知
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, userApply));
    }

    @Override
    public PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request) {
        IPage<UserApply> userApplyIPage = userApplyDao.friendApplyPage(uid, request.plusPage());
        if (CollectionUtil.isEmpty(userApplyIPage.getRecords())) {
            return PageBaseResp.empty();
        }
        //将这些申请列表设为已读
        readApples(uid, userApplyIPage);
        //返回消息
        return PageBaseResp.init(userApplyIPage, FriendAdapter.buildFriendApplyList(userApplyIPage.getRecords()));
    }

    /**
     * 把申请更新为已读
     */
    private void readApples(Long uid, IPage<UserApply> userApplyIPage) {
        List<Long> applyIds = userApplyIPage.getRecords()
                .stream().map(UserApply::getId)
                .collect(Collectors.toList());
        userApplyDao.readApples(uid, applyIds);
    }

    /**
     * 同意申请
     */
    @Override
    @Transactional
    public void applyApprove(Long uid, FriendApproveReq request) {
        UserApply userApply = userApplyDao.getById(request.getApplyId());
        if(Objects.isNull(userApply) || !userApply.getTargetId().equals(uid)){
            throw new BusinessException("申请不存在");
        }
        if(userApply.getStatus().equals(ApplyStatusEnum.AGREE.getCode())){
            throw new BusinessException("已经同意好友");
        }
        // 同意申请
        userApplyDao.agree(request.getApplyId());
        // 创建双方好友关系
        createFriend(uid, userApply.getUid());
        // TODO 创建一个聊天房间

        // TODO 发送一条好友添加成功的消息
    }

    @Override
    public FriendUnreadResp unread(Long uid) {
        return new FriendUnreadResp(userApplyDao.getUnreadCount(uid));
    }

    @Override
    public void deleteFriend(Long uid, Long targetUid) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, targetUid);
        if (CollectionUtil.isEmpty(userFriends)) {
            log.info("没有好友关系：{},{}", uid, targetUid);
            return;
        }
        List<Long> friendRecordIds = userFriends.stream().map(UserFriend::getId).collect(Collectors.toList());
        userFriendDao.removeByIds(friendRecordIds);
        // TODO 禁用双方的聊天房间

    }

    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq request) {
        List<UserFriend> userFriends = userFriendDao.getBatchUserFriend(uid, request.getUidList());
        return FriendCheckResp.check(request.getUidList(), userFriends);
    }

    /**
     * 创建双方好友关系
     */
    private void createFriend(Long uid, Long uid1) {
        UserFriend userFriend1 = UserFriend.builder()
                .friendUid(uid1)
                .uid(uid)
                .build();
        UserFriend userFriend2 = UserFriend.builder()
                .friendUid(uid)
                .uid(uid1)
                .build();
        userFriendDao.saveBatch(Lists.newArrayList(userFriend1, userFriend2));
    }
}

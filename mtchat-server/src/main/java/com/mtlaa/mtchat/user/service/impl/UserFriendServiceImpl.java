package com.mtlaa.mtchat.user.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.mtlaa.mtchat.chat.service.ChatService;
import com.mtlaa.mtchat.chat.service.RoomService;
import com.mtlaa.mtchat.domain.chat.entity.RoomFriend;
import com.mtlaa.mtchat.domain.chat.enums.MessageTypeEnum;
import com.mtlaa.mtchat.domain.chat.vo.request.ChatMessageReq;
import com.mtlaa.mtchat.domain.chat.vo.request.msg.TextMsgReq;
import com.mtlaa.mtchat.domain.common.vo.request.CursorPageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.request.PageBaseReq;
import com.mtlaa.mtchat.domain.common.vo.response.CursorPageBaseResp;
import com.mtlaa.mtchat.domain.common.vo.response.PageBaseResp;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.entity.UserApply;
import com.mtlaa.mtchat.domain.user.entity.UserFriend;
import com.mtlaa.mtchat.domain.user.enums.ApplyReadStatusEnum;
import com.mtlaa.mtchat.domain.user.enums.ApplyStatusEnum;
import com.mtlaa.mtchat.domain.user.enums.ApplyTypeEnum;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendApplyReq;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendApproveReq;
import com.mtlaa.mtchat.domain.user.vo.request.friend.FriendCheckReq;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendApplyResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendCheckResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendResp;
import com.mtlaa.mtchat.domain.user.vo.response.friend.FriendUnreadResp;
import com.mtlaa.mtchat.event.UserApplyEvent;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.user.dao.UserApplyDao;
import com.mtlaa.mtchat.user.dao.UserDao;
import com.mtlaa.mtchat.user.dao.UserFriendDao;
import com.mtlaa.mtchat.user.service.UserFriendService;
import com.mtlaa.mtchat.user.service.adapter.FriendAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
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
    @Autowired
    private RoomService roomService;
    @Autowired
    private ChatService chatService;


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
        List<FriendResp> friendResps = FriendAdapter.buildFriend(friendPage.getList(), friends);// 该适配器主要设置每个用户的在线状态
        return CursorPageBaseResp.init(friendPage, friendResps);
    }

    /**
     * 申请好友：判断是否已经申请、对方是否申请了自己
     *      最后发出申请好友事件，发送消息到MQ，websocket消费推送申请给对应用户
     */
    @Override
    @Transactional
    public void apply(Long uid, FriendApplyReq friendApplyReq) {
        if (uid.equals(friendApplyReq.getTargetUid())){
            throw new BusinessException("不可以申请自己");
        }
        // 判断是否已经是好友
        UserFriend userFriend = userFriendDao.getByUidAndFriendUid(uid, friendApplyReq.getTargetUid());
        if(userFriend != null){
            throw new BusinessException("已经是好友了");
        }
        // 判断是否已经申请了好友（还没同意）
        UserApply userApply = userApplyDao.getByUidAndTargetUidWithUnApply(uid, friendApplyReq.getTargetUid());
        if(Objects.nonNull(userApply)){
            log.info("已经申请过好友：uid：{}，targetUid：{}", uid, friendApplyReq.getTargetUid());
            throw new BusinessException("已经申请过啦~");
        }
        // 判断该targetId是否申请了自己
        userApply = userApplyDao.getByUidAndTargetUidWithUnApply(friendApplyReq.getTargetUid(), uid);
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
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userApplyDao.save(userApply);
        // 发出申请好友的事件，用于给被申请的人推送通知
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, userApply));
    }

    /**
     * mybatis-plus翻页，查询一页申请列表
     */
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
        createFriend(uid, userApply.getUid());  // 这样调用不会导致事务失效!! createFriend方法会加入到当前事务
        // 创建一个聊天房间: room表 与 room_friend表
        RoomFriend roomFriend = roomService.createFriendRoom(uid, userApply.getUid());
        // 发送一条好友添加成功的消息：使用消息模块发送消息，类似微信添加好哟发我们开始聊天
        chatService.sendMsg(uid, ChatMessageReq.builder()
                .roomId(roomFriend.getRoomId())
                .msgType(MessageTypeEnum.TEXT.getType())
                .body(TextMsgReq.builder().content("我们成为好友了，开始聊天吧~").build())
                .build());
    }

    /**
     * 返回当前用户被申请好友的未读数
     */
    @Override
    public FriendUnreadResp unread(Long uid) {
        return new FriendUnreadResp(userApplyDao.getUnreadCount(uid));
    }

    /**
     * 删除好友，删除实际好友关系（user_friend中的双向关系），最后禁用双方的聊天房间（逻辑删除）
     */
    @Override
    public void deleteFriend(Long uid, Long targetUid) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, targetUid);
        if (CollectionUtil.isEmpty(userFriends)) {
            log.info("没有好友关系：{},{}", uid, targetUid);
            return;
        }
        List<Long> friendRecordIds = userFriends.stream().map(UserFriend::getId).collect(Collectors.toList());
        userFriendDao.removeByIds(friendRecordIds);
        // 禁用双方的聊天房间
        roomService.disableFriendRoom(uid, targetUid);
    }

    /**
     * 批量校验目标uid是不是自己的好友
     */
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
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        UserFriend userFriend2 = UserFriend.builder()
                .friendUid(uid)
                .uid(uid1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        userFriendDao.saveBatch(Lists.newArrayList(userFriend1, userFriend2));
    }
}

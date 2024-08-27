package com.mtlaa.mtchat.user.service.impl;


import com.mtlaa.mtchat.cache.user.BlackCache;
import com.mtlaa.mtchat.cache.user.ItemCache;
import com.mtlaa.mtchat.cache.user.UserSummaryCache;
import com.mtlaa.mtchat.domain.user.dto.ItemInfoDTO;
import com.mtlaa.mtchat.domain.user.dto.SummeryInfoDTO;
import com.mtlaa.mtchat.domain.user.entity.Black;
import com.mtlaa.mtchat.domain.user.entity.ItemConfig;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.entity.UserBackpack;
import com.mtlaa.mtchat.domain.user.enums.BlackTypeEnum;
import com.mtlaa.mtchat.domain.user.enums.ItemEnum;
import com.mtlaa.mtchat.domain.user.enums.ItemTypeEnum;
import com.mtlaa.mtchat.domain.user.vo.request.ItemInfoReq;
import com.mtlaa.mtchat.domain.user.vo.request.ModifyNameRequest;
import com.mtlaa.mtchat.domain.user.vo.request.SummeryInfoReq;
import com.mtlaa.mtchat.domain.user.vo.response.BadgeResponse;
import com.mtlaa.mtchat.domain.user.vo.response.UserInfoResponse;
import com.mtlaa.mtchat.event.UserBlackEvent;
import com.mtlaa.mtchat.event.UserRegisterEvent;
import com.mtlaa.mtchat.exception.BusinessException;
import com.mtlaa.mtchat.user.dao.BlackDao;
import com.mtlaa.mtchat.user.dao.ItemConfigDao;
import com.mtlaa.mtchat.user.dao.UserBackpackDao;
import com.mtlaa.mtchat.user.dao.UserDao;
import com.mtlaa.mtchat.user.service.UserService;
import com.mtlaa.mtchat.user.service.adapter.UserAdapter;

import com.mtlaa.mtchat.cache.user.UserCache;
import com.mtlaa.mtchat.utils.sensitive.SensitiveWordFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Create 2023/12/6 16:28
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserBackpackDao userBackpackDao;
    @Autowired
    private ItemCache itemCache;
    @Autowired
    private ItemConfigDao itemConfigDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    // 批量缓存框架
    @Autowired
    private UserSummaryCache userSummaryCache;
    @Autowired
    private UserCache userCache;
    @Autowired
    private BlackDao blackDao;
    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;
    @Autowired
    private BlackCache blackCache;


    /**
     * 预注册，并发出新用户注册事件：异步发放初始勋章和改名卡
     */
    @Override
    @Transactional
    public Long register(User user) {
        userDao.save(user);
        userCache.refreshUserModifyTime(user.getId());
        // 发出用户注册的事件
        applicationEventPublisher.publishEvent(new UserRegisterEvent(this, user));
        return user.getId();
    }

    @Override
    public UserInfoResponse getUserInfo(Long uid) {
        UserInfoResponse infoResponse = new UserInfoResponse();
        // 获取用户信息
        User user = userDao.getById(uid);
        BeanUtils.copyProperties(user, infoResponse);
        // 获取改名次数
        Integer modifyN = userBackpackDao.getCountItemTypeWithValid(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        infoResponse.setModifyNameChance(modifyN);
        return infoResponse;
    }

    @Override
    @Transactional
    public void modifyName(Long uid, ModifyNameRequest modifyNameRequest) {
        // 判断名字里是否有敏感词
        if (sensitiveWordFilter.hasSensitiveWord(modifyNameRequest.getName())){
            throw new BusinessException("名字非法");
        }
        // 判断新名字是否重复
        User oldUser = userDao.getByName(modifyNameRequest.getName());
        if(Objects.nonNull(oldUser)){
            throw new BusinessException("名字重复了");
        }
        // 判断是否有改名卡
        UserBackpack userBackpack = userBackpackDao.getItemValid(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        if(userBackpack == null){
            throw new BusinessException("没有改名卡，无法改名");
        }
        userBackpackDao.useItem(userBackpack);
        // 修改
        User user = User.builder()
                .id(uid)
                .name(modifyNameRequest.getName())
                .updateTime(LocalDateTime.now())
                .build();
        userDao.updateById(user);
        userCache.refreshUserModifyTime(uid);
    }

    @Override
    public List<BadgeResponse> getUserBadges(Long uid) {
        // 获取所有徽章
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        // 获取用户拥有的徽章
        List<UserBackpack> userBackpacks = userBackpackDao.getByUidAndItemId(uid,
                itemConfigs.stream().map(ItemConfig::getId).collect(Collectors.toList()));
        // 获取用户佩戴的徽章
        User user = userDao.getById(uid);
        return UserAdapter.buildBadgesResponse(itemConfigs, userBackpacks, user);
    }

    @Override
    public void wearBadge(Long uid, Long itemId) {
        // 确保用户有这个徽章
        UserBackpack userBackpack = userBackpackDao.getByUidAndItemId(uid, itemId);
        if(userBackpack == null){
            throw new BusinessException("用户没有该物品: itemId = " + itemId);
        }
        // 确保该物品为徽章
        ItemConfig itemConfig = itemCache.getById(itemId);
        if(itemConfig == null || !itemConfig.getType().equals(ItemTypeEnum.BADGE.getType())){
            throw new BusinessException("不是徽章，无法佩戴: itemId = " + itemId);
        }
        // 佩戴徽章
        User user = User.builder()
                .id(uid)
                .itemId(itemId)
                .updateTime(LocalDateTime.now())
                .build();
        userDao.updateById(user);
        userCache.refreshUserModifyTime(uid);
    }

    /**
     * 同步指定用户uid的用户信息（如果需要）
     */
    @Override
    public List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req) {
        // 需要同步给前端用户信息的用户id集合
        List<Long> needSyncUids = getNeedSyncUidList(req.getReqList());
        // 从缓存获取需要同步的用户信息（加载用户信息）
        Map<Long, SummeryInfoDTO> batch = userSummaryCache.getBatch(needSyncUids);
        return req.getReqList()
                .stream()
                .map(a -> batch.containsKey(a.getUid()) ?
                        batch.get(a.getUid()) : SummeryInfoDTO.skip(a.getUid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 在Redis中缓存了每个uid最新更新的时间，前端会记录上一次同步的时间，本次同步根据这两个时间判断是否需要同步
     */
    private List<Long> getNeedSyncUidList(List<SummeryInfoReq.infoReq> reqList) {
        List<Long> needSyncUidList = new ArrayList<>();
        // 从缓存获取对应uid用户的更新时间
        List<Long> userModifyTime = userCache.getUserModifyTime(
                reqList.stream().map(SummeryInfoReq.infoReq::getUid).collect(Collectors.toList()));
        for (int i = 0; i < reqList.size(); i++) {
            SummeryInfoReq.infoReq infoReq = reqList.get(i);
            Long modifyTime = userModifyTime.get(i);
            if (Objects.isNull(infoReq.getLastModifyTime()) ||
                    Objects.isNull(modifyTime) || modifyTime > infoReq.getLastModifyTime()) {
                needSyncUidList.add(infoReq.getUid());
            }
        }
        return needSyncUidList;
    }

    /**
     * 同步指定用户uid的徽章信息（如果需要）
     * 同样是根据最后刷新的时间判断是否需要更新
     */
    @Override
    public List<ItemInfoDTO> getItemInfo(ItemInfoReq req) {
        return req.getReqList().stream().map(a -> {
            ItemConfig itemConfig = itemCache.getById(a.getItemId());  // 从本地缓存中查找徽章信息
            if (Objects.nonNull(a.getLastModifyTime()) &&
                    a.getLastModifyTime() >= itemConfig.getUpdateTime().toInstant(ZoneOffset.ofHours(8)).toEpochMilli()) {
                return ItemInfoDTO.skip(a.getItemId());
            }
            ItemInfoDTO dto = new ItemInfoDTO();
            dto.setItemId(itemConfig.getId());
            dto.setImg(itemConfig.getImg());
            dto.setDescribe(itemConfig.getDescribe());
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 拉黑一个人：拉黑uid和ip
     * <p>采用同步更新策略，先更新数据库，再更新缓存，如果缓存更新失败，会触发mysql回滚</p>
     * @param blackUid 被拉黑的uid
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blackUser(Long blackUid) {
        // 拉黑uid
        Black black = new Black();
        black.setType(BlackTypeEnum.UID.getType());
        black.setTarget(blackUid.toString());
        black.setCreateTime(LocalDateTime.now());
        black.setUpdateTime(LocalDateTime.now());
        blackDao.save(black);
        // 拉黑ip
        User user = userDao.getById(blackUid);
        blackIp(user.getIpInfo().getCreateIp());
        if(!Objects.equals(user.getIpInfo().getCreateIp(), user.getIpInfo().getUpdateIp())) {
            blackIp(user.getIpInfo().getUpdateIp());
        }
        blackCache.updateRedisBlack(blackUid, user.getIpInfo().getCreateIp(), user.getIpInfo().getUpdateIp());
        // 发出拉黑一个人的事件
        applicationEventPublisher.publishEvent(new UserBlackEvent(this, user));
    }
    public void blackIp(String ip){
        if(StringUtils.isBlank(ip) || ip.equals("127.0.0.1") || ip.startsWith("192.168")){
            return;
        }
        Black black = new Black();
        black.setType(BlackTypeEnum.IP.getType());
        black.setTarget(ip);
        black.setCreateTime(LocalDateTime.now());
        black.setUpdateTime(LocalDateTime.now());
        blackDao.save(black);
    }

}

package com.mtlaa.mychat.user.service.impl;

import com.mtlaa.mychat.common.event.UserRegisterEvent;
import com.mtlaa.mychat.common.exception.BusinessException;
import com.mtlaa.mychat.user.dao.ItemConfigDao;
import com.mtlaa.mychat.user.dao.UserBackpackDao;
import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.ItemConfig;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.domain.entity.UserBackpack;
import com.mtlaa.mychat.user.domain.enums.ItemEnum;
import com.mtlaa.mychat.user.domain.enums.ItemTypeEnum;
import com.mtlaa.mychat.user.domain.vo.request.ModifyNameRequest;
import com.mtlaa.mychat.user.domain.vo.response.BadgeResponse;
import com.mtlaa.mychat.user.domain.vo.response.UserInfoResponse;
import com.mtlaa.mychat.user.service.UserService;
import com.mtlaa.mychat.user.service.adapter.UserAdapter;
import com.mtlaa.mychat.user.service.cache.ItemCache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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


    @Override
    @Transactional
    public Long register(User user) {
        userDao.save(user);
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
        ItemConfig itemConfig = itemConfigDao.getById(itemId);
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
    }
}

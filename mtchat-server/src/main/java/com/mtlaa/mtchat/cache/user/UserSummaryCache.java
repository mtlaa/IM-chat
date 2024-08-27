package com.mtlaa.mtchat.cache.user;


import com.mtlaa.mtchat.cache.AbstractRedisStringCache;
import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.domain.user.dto.SummeryInfoDTO;
import com.mtlaa.mtchat.domain.user.entity.ItemConfig;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.entity.UserBackpack;
import com.mtlaa.mtchat.domain.user.enums.ItemTypeEnum;
import com.mtlaa.mtchat.domain.user.entity.IpDetail;
import com.mtlaa.mtchat.domain.user.entity.IpInfo;
import com.mtlaa.mtchat.user.dao.UserBackpackDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Create 2023/12/22 16:12
 */
@Component
public class UserSummaryCache extends AbstractRedisStringCache<Long, SummeryInfoDTO> {
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private ItemCache itemCache;
    @Autowired
    private UserBackpackDao userBackpackDao;

    @Override
    protected String getKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_SUMMARY_STRING, uid);
    }

    @Override
    protected Map<Long, SummeryInfoDTO> load(List<Long> uids) {
        Map<Long, User> userInfo = userInfoCache.load(uids);
        // 获取用户徽章信息
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());  // 获取所有徽章
        List<Long> itemIds = itemConfigs.stream().map(ItemConfig::getId).collect(Collectors.toList()); // 获取所有徽章的id
        List<UserBackpack> userBackpacks = userBackpackDao.getByItemIds(uids, itemIds);
        Map<Long, List<UserBackpack>> userBadges = userBackpacks.stream()
                .collect(Collectors.groupingBy(UserBackpack::getUid));

        return uids.stream().map(uid -> {
            SummeryInfoDTO summeryInfoDTO = new SummeryInfoDTO();
            User user = userInfo.get(uid);
            if(Objects.isNull(user)){
                return null;
            }
            summeryInfoDTO.setUid(uid);
            summeryInfoDTO.setName(user.getName());
            summeryInfoDTO.setAvatar(user.getAvatar());
            summeryInfoDTO.setWearingItemId(user.getItemId());
            summeryInfoDTO.setLocPlace(Optional.ofNullable(user.getIpInfo())
                    .map(IpInfo::getUpdateIpDetail)
                    .map(IpDetail::getCity).orElse(null));
            summeryInfoDTO.setItemIds(userBadges.getOrDefault(uid, new ArrayList<>())
                    .stream().map(UserBackpack::getItemId).collect(Collectors.toList()));
            return summeryInfoDTO;
        }).filter(Objects::nonNull).collect(Collectors.toMap(SummeryInfoDTO::getUid, Function.identity()));
    }

    @Override
    protected Long getExpireSeconds() {
        return 15 * 60L;
    }
}

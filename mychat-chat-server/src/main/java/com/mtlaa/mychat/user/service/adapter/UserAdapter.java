package com.mtlaa.mychat.user.service.adapter;

import com.mtlaa.mychat.user.domain.entity.ItemConfig;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.domain.entity.UserBackpack;
import com.mtlaa.mychat.user.domain.vo.response.BadgeResponse;
import org.springframework.beans.BeanUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create 2023/12/12 16:06
 */
public class UserAdapter {
    public static List<BadgeResponse> buildBadgesResponse(List<ItemConfig> itemConfigs,
                                                          List<UserBackpack> userBackpacks,
                                                          User user){
        Set<Long> itemIdSet = userBackpacks.stream().map(UserBackpack::getItemId).collect(Collectors.toSet());
        return itemConfigs.stream().map(itemConfig -> {
                    BadgeResponse badgeResponse = new BadgeResponse();
                    BeanUtils.copyProperties(itemConfig, badgeResponse);
                    badgeResponse.setObtain(itemIdSet.contains(itemConfig.getId()) ? 1 : 0);
                    badgeResponse.setWearing(Objects.equals(user.getItemId(), itemConfig.getId()) ? 1 : 0);
                    return badgeResponse;
                }).sorted(Comparator.comparing(BadgeResponse::getWearing, Comparator.reverseOrder())
                        .thenComparing(BadgeResponse::getObtain, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}

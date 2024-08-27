package com.mtlaa.mtchat.user.service;


import com.mtlaa.mtchat.domain.user.dto.ItemInfoDTO;
import com.mtlaa.mtchat.domain.user.dto.SummeryInfoDTO;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.domain.user.vo.request.ItemInfoReq;
import com.mtlaa.mtchat.domain.user.vo.request.ModifyNameRequest;
import com.mtlaa.mtchat.domain.user.vo.request.SummeryInfoReq;
import com.mtlaa.mtchat.domain.user.vo.response.BadgeResponse;
import com.mtlaa.mtchat.domain.user.vo.response.UserInfoResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author mtlaa
 * @since 2023-11-30
 */
@Service
public interface UserService {

    Long register(User user);

    UserInfoResponse getUserInfo(Long uid);

    void modifyName(Long uid, ModifyNameRequest modifyNameRequest);

    List<BadgeResponse> getUserBadges(Long uid);

    void wearBadge(Long uid, Long itemId);

    List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req);

    List<ItemInfoDTO> getItemInfo(ItemInfoReq req);

    void blackUser(Long blackUid);
}

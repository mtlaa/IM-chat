package com.mtlaa.mychat.user.service;

import com.mtlaa.mychat.user.domain.dto.ItemInfoDTO;
import com.mtlaa.mychat.user.domain.dto.SummeryInfoDTO;
import com.mtlaa.mychat.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mtlaa.mychat.user.domain.vo.request.ItemInfoReq;
import com.mtlaa.mychat.user.domain.vo.request.ModifyNameRequest;
import com.mtlaa.mychat.user.domain.vo.request.SummeryInfoReq;
import com.mtlaa.mychat.user.domain.vo.response.BadgeResponse;
import com.mtlaa.mychat.user.domain.vo.response.UserInfoResponse;
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
}

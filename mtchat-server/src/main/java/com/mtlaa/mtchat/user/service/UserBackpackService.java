package com.mtlaa.mtchat.user.service;

import com.mtlaa.mtchat.domain.user.enums.IdempotentEnum;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户背包表 服务类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-11
 */
@Service
public interface UserBackpackService{
    /**
     * 给用户发放一个物品
     * @param uid uid
     * @param itemId 物品id
     * @param idempotentEnum 幂等类型
     * @param businessId 幂等唯一标识
     */
    void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId);
}

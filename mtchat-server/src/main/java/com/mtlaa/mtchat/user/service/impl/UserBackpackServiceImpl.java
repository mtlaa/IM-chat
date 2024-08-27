package com.mtlaa.mtchat.user.service.impl;


import com.mtlaa.mtchat.annotation.RedissonLock;
import com.mtlaa.mtchat.domain.user.entity.UserBackpack;
import com.mtlaa.mtchat.domain.user.enums.IdempotentEnum;
import com.mtlaa.mtchat.user.dao.UserBackpackDao;
import com.mtlaa.mtchat.user.service.UserBackpackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Create 2023/12/13 9:43
 */
@Service
public class UserBackpackServiceImpl implements UserBackpackService {
    @Autowired
    private UserBackpackDao userBackpackDao;

    /**
     * 采用分布式锁，避免多个请求重复发放一个物品
     * @param uid uid
     * @param itemId 物品id
     * @param idempotentEnum 幂等类型
     * @param businessId 幂等唯一标识，如用户id、消息id
     */
    @Override
    @RedissonLock(key = "#uid + '_' + #itemId", waitTime = 2000)  // 分布式锁注解
    @Transactional
    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId){
        String idempotent = getIdempotent(itemId, idempotentEnum, businessId);

        UserBackpack userBackpack = userBackpackDao.getByIdempotent(idempotent);
        if(userBackpack != null){
            return;  // 算发放成功，但不重复发放
        }
        userBackpack = userBackpackDao.getByUidAndItemId(uid, itemId);
        if(userBackpack != null){
            return;  // 不是本次发放的，不重复发放
        }
        // 发放物品
        userBackpack = UserBackpack.builder()
                .uid(uid)
                .itemId(itemId)
                .idempotent(idempotent)
                .createTime(LocalDateTime.now()).updateTime(LocalDateTime.now())
                .status(0).build();
        userBackpackDao.save(userBackpack);
    }

    private String getIdempotent(Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        return String.format("%d_%d_%s", itemId, idempotentEnum.getType(), businessId);
    }
}

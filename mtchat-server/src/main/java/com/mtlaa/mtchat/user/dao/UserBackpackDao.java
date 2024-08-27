package com.mtlaa.mtchat.user.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtlaa.mtchat.domain.user.entity.UserBackpack;
import com.mtlaa.mtchat.user.mapper.UserBackpackMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户背包表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-11
 */
@Service
public class UserBackpackDao extends ServiceImpl<UserBackpackMapper, UserBackpack> {

    public Integer getCountItemTypeWithValid(Long uid, Long itemId) {
        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, 0).count();
    }


    public void useItem(UserBackpack userBackpack) {
        lambdaUpdate().eq(UserBackpack::getId, userBackpack.getId())
                .eq(UserBackpack::getStatus, 0)
                .set(UserBackpack::getStatus, 1).update();
    }

    public UserBackpack getItemValid(Long uid, Long itemId) {
        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, 0)
                .orderByAsc(UserBackpack::getId)
                .last("limit 1")
                .one();
    }

    public List<UserBackpack> getByUidAndItemId(Long uid, List<Long> itemIds) {
        return lambdaQuery()
                .eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getStatus, 0)
                .in(UserBackpack::getItemId, itemIds)
                .list();
    }
    public UserBackpack getByUidAndItemId(Long uid, Long itemId){
        return lambdaQuery().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .one();
    }

    public UserBackpack getByIdempotent(String idempotent) {
        return lambdaQuery()
                .eq(UserBackpack::getIdempotent, idempotent)
                .one();
    }

    public List<UserBackpack> getByItemIds(List<Long> uids, List<Long> itemIds) {
        return lambdaQuery().in(UserBackpack::getUid, uids)
                .in(UserBackpack::getItemId, itemIds)
                .eq(UserBackpack::getStatus, 0)
                .list();
    }
}

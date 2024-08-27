package com.mtlaa.mtchat.user.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mtlaa.mtchat.domain.user.entity.ItemConfig;
import com.mtlaa.mtchat.user.mapper.ItemConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 功能物品配置表 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-11
 */
@Service
public class ItemConfigDao extends ServiceImpl<ItemConfigMapper, ItemConfig> {

    public List<ItemConfig> getByType(Integer itemType) {
        return lambdaQuery().eq(ItemConfig::getType, itemType).list();
    }
}

package com.mtlaa.mychat.user.dao;

import com.mtlaa.mychat.user.domain.entity.ItemConfig;
import com.mtlaa.mychat.user.mapper.ItemConfigMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

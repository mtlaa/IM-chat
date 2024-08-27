package com.mtlaa.mtchat.cache.user;


import com.mtlaa.mtchat.domain.user.entity.ItemConfig;
import com.mtlaa.mtchat.user.dao.ItemConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Create 2023/12/12 15:34
 * 管理物品缓存的类
 */
@Component
public class ItemCache {
    @Autowired
    private ItemConfigDao itemConfigDao;

    /**
     * @Cacheable 注解 在调用该方法前，会查看是否有指定的缓存，如果有则直接返回缓存
     * 如果没有则执行方法，把返回值加入缓存
     */
    @Cacheable(cacheNames = "item", key = "'itemByType:' + #itemType")  // 最终key为：itemByType:0
    public List<ItemConfig> getByType(Integer itemType){
        return itemConfigDao.getByType(itemType);
    }

    /**
     * @CacheEvict  调用该方法会删除指定缓存
     */
    @CacheEvict(cacheNames = "item", key = "'itemByType:' + #itemType")
    public void evictItemCache(Integer itemType){
    }

    @Cacheable(cacheNames = "item", key = "'itemByType:' + #itemId")
    public ItemConfig getById(Long itemId) {
        return itemConfigDao.getById(itemId);
    }
    @CacheEvict(cacheNames = "item", key = "'itemByType:' + #itemId")
    public void evictItemCache(Long itemId){
    }
}

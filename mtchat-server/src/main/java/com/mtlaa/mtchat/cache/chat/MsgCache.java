package com.mtlaa.mtchat.cache.chat;

import com.mtlaa.mtchat.cache.AbstractRedisStringCache;
import com.mtlaa.mtchat.chat.dao.MessageDao;
import com.mtlaa.mtchat.constant.RedisKey;
import com.mtlaa.mtchat.domain.chat.entity.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 消息缓存, 使用 二级缓存
 */
@Component
@CacheConfig(cacheManager = "msgCacheManager")  // 本地缓存配置了 10min 的过期时间
public class MsgCache extends AbstractRedisStringCache<Long, Message> {  // msgId -- Message
    @Autowired
    private MessageDao messageDao;

    /**
     * ‘@Cacheable’当方法的返回为null时，默认也会缓存下来。使用 ‘unless’ 避免缓存null
     * @param msgId id
     * @return 消息体
     */
    @Cacheable(cacheNames = "msg", key = "'msg'+#msgId", unless = "#result == null")
    public Message getMsg(Long msgId) {
        return this.get(msgId);
    }

    @CacheEvict(cacheNames = "msg", key = "'msg'+#msgId")
    public Message evictMsg(Long msgId) {
        this.delete(msgId);
        return null;
    }

    /**
     * 更新数据库，并且删除缓存
     */
    @CacheEvict(cacheNames = "msg", key = "'msg'+#message.id")
    public void updateById(Message message){
        message.setUpdateTime(new Date());
        messageDao.updateById(message);

        this.delete(message.getId());
    }

    /**
     * Redis缓存消息的key
     */
    @Override
    protected String getKey(Long msgId) {
        return RedisKey.getKey(RedisKey.MSG_INFO_STRING, msgId);
    }

    /**
     * 从数据库读取消息
     */
    @Override
    protected Map<Long, Message> load(List<Long> msgIds) {
        List<Message> messages = messageDao.listByIds(msgIds);
        return messages.stream().collect(Collectors.toMap(Message::getId, Function.identity()));
    }

    /**
     * 过期时间 1h
     */
    @Override
    protected Long getExpireSeconds() {
        return 60 * 60L;
    }
}

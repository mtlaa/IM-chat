package com.mtlaa.mtchat.cache;

import cn.hutool.core.collection.CollectionUtil;

import com.mtlaa.mtchat.utils.redis.RedisUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采用旁路缓存的批量缓存框架
 */
public abstract class AbstractRedisStringCache<IN, OUT> implements BatchCache<IN, OUT> {
    private Class<OUT> outClass;

    protected AbstractRedisStringCache() {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.outClass = (Class<OUT>) genericSuperclass.getActualTypeArguments()[1];
    }

    /**
     * 生成缓存的 key
     */
    protected abstract String getKey(IN in);

    /**
     * 定义从数据库查询数据的逻辑
     */
    protected abstract Map<IN, OUT> load(List<IN> req);

    /**
     * 设置缓存的过期时间
     */
    protected abstract Long getExpireSeconds();

    /**
     * 从Redis中获取单个缓存，复用获取批量的方法
     */
    @Override
    public OUT get(IN req) {
        return getBatch(Collections.singletonList(req)).get(req);
    }

    /**
     * 旁路缓存（批量缓存框架）：批量从Redis查询缓存，或从数据库查询数据更新缓存
     * <p>req: roomId</p>
     */
    @Override
    public Map<IN, OUT> getBatch(List<IN> req) {
        // 如果请求获取列表为空，直接返回
        if(CollectionUtil.isEmpty(req)){
            return new HashMap<>();
        }
        // 不空，去重
        req = req.stream().distinct().collect(Collectors.toList());
        // 把req转为redis的Key
        List<String> reqKeys = req.stream().map(this::getKey).collect(Collectors.toList());
        // 从Redis批量获取缓存
        List<OUT> loadCache = RedisUtils.mget(reqKeys, outClass);
        // 筛选出没有获取到缓存的key
        List<IN> needReload = new ArrayList<>();
        for(int i=0;i<loadCache.size();i++){
            if(Objects.isNull(loadCache.get(i))){
                needReload.add(req.get(i));
            }
        }
        // 重新查找数据库，然后加入缓存
        Map<IN, OUT> reload = new HashMap<>();
        if(CollectionUtil.isNotEmpty(needReload)){
            reload = load(needReload);  // 从数据库获取的
            Map<String, OUT> cache = reload.entrySet().stream()
                    .map(a -> Pair.of(getKey(a.getKey()), a.getValue())) // 把数据库的key转为Redis的key
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            RedisUtils.mset(cache, getExpireSeconds());
        }
        // 合并数据
        Map<IN, OUT> resultMap = new HashMap<>();
        for(int i=0;i<req.size();i++){
            IN in = req.get(i);
            OUT out = Optional.ofNullable(loadCache.get(i))
                    .orElse(reload.get(in));
            resultMap.put(in, out);
        }
        return resultMap;
    }

    /**
     * 从Redis中删除当个缓存，复用删除批量的方法
     */
    @Override
    public void delete(IN req) {
        deleteBatch(Collections.singletonList(req));
    }

    @Override
    public void deleteBatch(List<IN> req) {
        List<String> keys = req.stream().map(this::getKey).collect(Collectors.toList());
        RedisUtils.del(keys);
    }

    /**
     * 删除数据库、同时删除缓存。默认实现为--只删除缓存
     * @param req id
     */
    public void remove(IN req){
        delete(req);
    }
}

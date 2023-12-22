package com.mtlaa.mychat.common.service.cache;

import cn.hutool.core.collection.CollectionUtil;
import com.mtlaa.redis.utils.RedisUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Create 2023/12/22 15:11
 */
public abstract class AbstractRedisStringCache<IN, OUT> implements BatchCache<IN, OUT> {
    private Class<OUT> outClass;

    protected AbstractRedisStringCache() {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.outClass = (Class<OUT>) genericSuperclass.getActualTypeArguments()[1];
    }

    protected abstract String getKey(IN in);
    protected abstract Map<IN, OUT> load(List<IN> req);
    protected abstract Long getExpireSeconds();

    /**
     * 从Redis中获取当个缓存，复用获取批量的方法
     */
    @Override
    public OUT get(IN req) {
        return getBatch(Collections.singletonList(req)).get(req);
    }

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
                    .map(a -> Pair.of(getKey(a.getKey()), a.getValue()))
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
}

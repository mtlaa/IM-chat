package com.mtlaa.mtchat.user.service.impl;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import com.mtlaa.mtchat.cache.user.UserCache;
import com.mtlaa.mtchat.domain.common.vo.response.ApiResult;
import com.mtlaa.mtchat.domain.user.entity.IpDetail;
import com.mtlaa.mtchat.domain.user.entity.IpInfo;
import com.mtlaa.mtchat.domain.user.entity.User;
import com.mtlaa.mtchat.user.dao.UserDao;
import com.mtlaa.mtchat.user.service.IpService;
import com.mtlaa.mtchat.utils.redis.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Create 2023/12/15 17:28
 */
@Service
@Slf4j
public class IpServiceImpl implements IpService {

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(500),
            new NamedThreadFactory("refresh-ipDetail", false));

    @Autowired
    private UserDao userDao;
    @Autowired
    private UserCache userCache;

    @Override
    public void refreshIpDetailAsync(Long uid) {
        EXECUTOR.execute(() -> {
            User user = userDao.getById(uid);
            IpInfo ipInfo = user.getIpInfo();
            if(ipInfo == null) return;
            String ip = ipInfo.needRefreshIp();
            if(StringUtils.isBlank(ip)) return;
            // 需要刷新
            IpDetail ipDetail = tryGetIpDetailOrNullTreeTimes(ip);
            if(ipDetail != null){
                ipInfo.refreshIpDetail(ipDetail);
                User update = User.builder()
                        .updateTime(LocalDateTime.now())
                        .id(uid)
                        .ipInfo(ipInfo)
                        .build();
                userDao.updateById(update);
                userCache.refreshUserModifyTime(uid);
            }else{
                log.info("解析ip失败：{}", ip);
            }
        });
    }

    private IpDetail tryGetIpDetailOrNullTreeTimes(String ip) {
        for(int i=0;i<3;i++){
            IpDetail ipDetail = GetIpDetailOrNull(ip);
            if(ipDetail != null){
                log.info("解析ip成功：{}", ipDetail);
                return ipDetail;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error("tryGetIpDetailOrNullTreeTimes:", e);
            }
        }
        return null;
    }

    /**
     * 执行解析IP
     * @param ip 需要解析的ip
     * @return ipDetail 解析的结果
     */
    private IpDetail GetIpDetailOrNull(String ip) {
        String requestUrl = "https://ip.taobao.com/outGetIpInfo?ip=" + ip + "&accessKey=alibaba-inc";
        String dataJson = HttpUtil.get(requestUrl);
        ApiResult<IpDetail> result = JsonUtils.toObj(dataJson, new TypeReference<ApiResult<IpDetail>>() {});
        if(result == null ) return null;
        return result.getData();
    }
}

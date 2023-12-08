package com.mtlaa.mychat;

import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import com.mtlaa.mychat.user.service.LoginService;
import com.mtlaa.redis.utils.RedisUtils;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Create 2023/11/30 18:06
 */
@SpringBootTest
public class SpringTest {
    @Autowired
    private UserDao userDao;
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private LoginService loginService;


    @Test
    public void testRedis(){
        RedisUtils.set("name", 1234);
        Integer value = RedisUtils.get("name", Integer.class);
        System.out.println(value);
    }
    @Test
    public void testRedisson(){
        RLock lock = redissonClient.getLock("lock1");
        lock.lock();
        System.out.println();
        lock.unlock();

    }
    @Test
    public void testLogin(){
        String s = "eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MTEwMDMsImV4cCI6MTcwMTg2ODEyMX0.3xa7BYLJC2dLOMGFe3gnGarJJgcps8mdvKBGq2YC3LY";
        Long uid = loginService.getValidUid(s);
        System.out.println(uid);

    }

    @Test
    public void testUserDao(){
//        User user = userDao.getById(1);
//        System.out.println(user);
//        System.out.println(userDao.count());
//        user = new User();
//        user.setName("test");
//        user.setOpenId("asdfwe");
//        System.out.println(userDao.save(user));
        userDao.remove(null);
        System.out.println(userDao.count());
    }

    @Test
    public void testWxSDK() throws WxErrorException {
        String url = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(111, 1000).getUrl();
        System.out.println(url);
    }
}

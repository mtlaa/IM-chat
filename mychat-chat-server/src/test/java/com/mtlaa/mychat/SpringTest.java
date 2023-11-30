package com.mtlaa.mychat;

import com.mtlaa.mychat.user.dao.UserDao;
import com.mtlaa.mychat.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Create 2023/11/30 18:06
 */
@SpringBootTest
public class SpringTest {
    @Autowired
    private UserDao userDao;

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
}

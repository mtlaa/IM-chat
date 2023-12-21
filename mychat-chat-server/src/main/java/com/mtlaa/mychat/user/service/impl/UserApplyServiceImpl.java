package com.mtlaa.mychat.user.service.impl;

import com.mtlaa.mychat.user.dao.UserApplyDao;
import com.mtlaa.mychat.user.service.UserApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create 2023/12/21 10:59
 */
@Service
public class UserApplyServiceImpl implements UserApplyService {
    @Autowired
    private UserApplyDao userApplyDao;

}

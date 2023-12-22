package com.mtlaa.mychat.user.dao;

import com.mtlaa.mychat.user.domain.entity.Black;
import com.mtlaa.mychat.user.mapper.BlackMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 黑名单 服务实现类
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-22
 */
@Service
public class BlackDao extends ServiceImpl<BlackMapper, Black> {

}

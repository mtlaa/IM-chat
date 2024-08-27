package com.mtlaa.mtchat.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mtlaa.mtchat.domain.user.entity.UserApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户申请表 Mapper 接口
 * </p>
 *
 * @author mtlaa
 * @since 2023-12-21
 */
@Mapper
public interface UserApplyMapper extends BaseMapper<UserApply> {

}

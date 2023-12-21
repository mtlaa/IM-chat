package com.mtlaa.mychat.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Create 2023/12/21 11:16
 * 配置 MP的自动填充字段：
 *      1、在实体类的相应字段上添加注解，指明 fill = FieldFill.INSERT 或 fill = FieldFill.INSERT_UPDATE
 *      2、定义以下bean，实现元对象处理类，用于处理字段的自动填充
 * 不配置该类就无法自动填充字段（在代码生成器代码中配置自动填充字段只是会添加实体类上的注解）
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}

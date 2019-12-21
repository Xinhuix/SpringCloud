package com.visionvera.context.annotations;

import com.visionvera.enums.PushUserTypeEnum;

import java.lang.annotation.*;

/**
 * 运维工作站添加用户、修改用户、删除用户的时候，向其他平台(会易通、网管、一机一档)推送对应的用户信息
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface PushUserForOtherPlatformAnnotation {
    /** 操作类型: 使用枚举PushUserTypeEnum */
    PushUserTypeEnum type();
}

package com.haothink.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 权限验证，，用于方法上<br/>
 * privilegeUrl 表示拦截的URL
 * PrivilegeReturnTypeEnum 没有权限访问时返回类型，ajax提交和正常页面提交
 * </p>
 *
 * @author: wanghao
 * @date: 2014-9-4
 */


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedPrivilege {
    String privilegeUrl() default "";

    int privilegeSid() default 0;

//    PrivilegeReturnTypeEnum accessType() default PrivilegeReturnTypeEnum.page;
}

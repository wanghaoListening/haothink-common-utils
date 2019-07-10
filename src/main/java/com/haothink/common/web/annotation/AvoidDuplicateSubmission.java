package com.haothink.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 防止重复提交注解，用于方法上<br/>
 * 在新建页面方法上，设置createSessionToken()为true，此时拦截器会在Session中保存一个token，
 * validateDuplicateSubmit 表示拦截是否重复提交
 * pageSubmitType true正常的页面表单提交,false为ajax表单提交
 * 同时需要在新建的页面中添加
 * <input type="hidden" name="token" value="${token}">
 * <br/>
 * 保存方法需要验证重复提交的，设置createSessionToken为true
 * 此时会在拦截器中验证是否重复提交,并将session中得token清除
 * </p>
 * @author: wanghao
 * @date: 2014-3-21
 *
 */


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AvoidDuplicateSubmission {
    boolean createToken() default false;//创建token
    String tokenName() default "default";//创建token的名称，避免token被覆盖
    boolean validToken() default false;//验证token
    boolean isAjax() default false;//提交方式 是否是ajax
}

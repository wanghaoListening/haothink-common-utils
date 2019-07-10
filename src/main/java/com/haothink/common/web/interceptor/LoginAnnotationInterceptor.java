package com.haothink.common.web.interceptor;

import com.haothink.common.constants.Constants;
import com.haothink.common.utils.CookieUtils;
import com.haothink.common.web.annotation.NotNeedCheckLogin;
import com.haothink.common.web.session.AbstractSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 对使用Login注解的类进行拦截，正对不同设置进行相应的返回
 * 对于正常表单提交（非AJAX）跳转到登录页面，对于ajax提交的请求返回ajax数据
 *
 * @author wanghao
 */
public class LoginAnnotationInterceptor extends AbstractLoginInterceptor {
    /**
     * 登陆URL
     * 默认为login.jsp,可以通过spring配置文件通过属性注入，更改登陆页面的位置，如：
     * <beans class="LoginAnnotationInterceptor">
     * <property name="loginUrl"  value="/view/admin/login.jsp"/>
     * </beans>
     */
    private String loginUrl;

    /**
     * cookie的域名
     * 跨域使用
     */
    private String domain;

    /**
     * cookie和cache 的过期时间 单位：秒
     * 每次有效访问之后会设置
     */
    private int expireTime;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        //CORS 请求预处理阶段不走拦截器
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        logger.debug("----进入LoginAnnotationInterceptor 拦截器 ----");
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        NotNeedCheckLogin notNeedCheckLogin = null;
        if (handlerMethod.getMethod().isAnnotationPresent(NotNeedCheckLogin.class)) {
            notNeedCheckLogin = handlerMethod.getMethodAnnotation(NotNeedCheckLogin.class);
        }

        showDebugInfo(request);

        if (null != notNeedCheckLogin) {
            logger.debug(String.format("----本次请求不需要检查登陆----"));
            return true;
        }

        //获取用户登陆信息
        AbstractSession session = sessionService.getSession(request);
        //TODO 需要根据不同系统，区分不同cookieName
        Cookie loginCookie = CookieUtils.getCookieByName(request, Constants.COOKIE_LOGIN_USER_ID);
        if (!isUserLogin(loginCookie, session)) {
            logger.debug(String.format("----非登陆状态 ----"));
            loginErrorRedirect(request, response, loginUrl);
            return false;
        }

        //重置session 和 cookie的expire time
        sessionService.extendExpireTime(session.getSessionId(), expireTime);
        logger.debug(String.format("[----Old]CookieName:[%s]value[%s]domain[%s]expireTime[%s]", loginCookie.getName(), loginCookie.getValue(), loginCookie.getDomain(), loginCookie.getMaxAge()));

        //重置用户user login cookie过期时间
        CookieUtils.resetCookieExpireTime(response, loginCookie, expireTime, domain);

        return true;
    }


    private void showDebugInfo(HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            //ajax 为 XMLHttpRequest
            String requestType = request.getHeader("X-Requested-With");
            boolean isAjax = requestType == null ? false : true;
            String requestURL = request.getRequestURL().toString();
            String reqParam = request.getQueryString();
            boolean isJsonp = StringUtils.isNotBlank(request.getParameter("callback"));
            logger.debug(String.format("----请求信息request url[%s]params[%s]reqType[%s]isAjax[%s]isJsonp[%s] ----", requestURL, reqParam, requestType, isAjax, isJsonp));
        }
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

}

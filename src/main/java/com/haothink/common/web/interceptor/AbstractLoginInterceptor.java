package com.haothink.common.web.interceptor;

import com.haothink.common.constants.Constants;
import com.haothink.common.domain.Result;
import com.haothink.common.utils.CookieUtils;
import com.haothink.common.web.session.AbstractSession;
import com.haothink.common.web.session.SessionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * 登陆拦截器
 * @author wanghao
 * @date 16/5/26
 */
public class AbstractLoginInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    SessionService sessionService;

    protected static final Logger logger = LoggerFactory.getLogger(LoginAnnotationInterceptor.class);
    public int ERROR_CODE;
    public String ERROR_MSG;
    public static final int NOT_LOGIN = -100;
    public static final String NOT_LOGIN_MSG = "您的登录已过期，请返回重新登录!";
    public static final int DUPLICATION_SUBMIT = -101;
    public static final String DUPLICATION_SUBMIT_MSG = "你的请求已提交，请不要重复提交";

    protected void loginErrorRedirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl) {
        ERROR_CODE = NOT_LOGIN;
        ERROR_MSG = NOT_LOGIN_MSG;
        _sendRedirect(request, response, redirectUrl);
    }


    protected void duplicationSubmitErrorRedirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl) {
        ERROR_CODE = DUPLICATION_SUBMIT;
        ERROR_MSG = DUPLICATION_SUBMIT_MSG;
        _sendRedirect(request, response, redirectUrl);
    }

    /**
     * @param request
     * @param response
     * @param redirectUrl
     */
    protected void _sendRedirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl) {
        boolean isAjax = request.getHeader("x-requested-with") == null ? false : true;
        //前端强制使用ajax
        boolean forceAjax = request.getParameter("ajax") == null ? false : true;
        String callback = request.getParameter("callback");
        boolean isJsonp = StringUtils.isNotBlank(callback);
        if (isAjax || forceAjax) {
            ajaxRedirect(request, response);
        } else if (isJsonp) {
            ajaxJsonpRedirect(request, response);
        } else {
            try {
                response.sendRedirect(redirectUrl);
            } catch (Exception e) {
                logger.error("=====用户过期，跳转页面时出错======", e);
            }
        }
    }

    private void ajaxRedirect(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=UTF-8");
        OutputStream out = null;
        try {
            String fromUrl = request.getHeader("referer");
            out = response.getOutputStream();
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "utf-8"));
            Result message = Result.buildFailedResult(fromUrl, ERROR_CODE);
            pw.println(message.toString());
            pw.flush();
            pw.close();
        } catch (IOException e) {
            logger.error("===== ajax output error!~", e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.error("=====  output close!~", e);
            }
        }
    }


    private void ajaxJsonpRedirect(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=UTF-8");
        ServletOutputStream out = null;
        try {
            String fromUrl = request.getHeader("referer");
            String callback = request.getParameter("callback");
            out = response.getOutputStream();
            PrintWriter e = new PrintWriter(new OutputStreamWriter(out, "utf-8"));
            Result message = Result.buildFailedResult(fromUrl, ERROR_CODE);
            StringBuffer json = new StringBuffer();
            json.append(callback);
            json.append("(");
            json.append(message.toString());
            json.append(")");
            e.println(json.toString());
            e.flush();
            e.close();
        } catch (IOException e) {
            logger.error("===== ajax jsonp output error!~", e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.error("=====  output close!~", e);
            }
        }
    }

    /**
     * 判断用户是否正常登陆
     *
     * @param request
     * @return true：正常登陆用户 false:未登陆or非正常登陆用户
     */
    protected boolean isUserLogin(HttpServletRequest request) {
        Cookie loginCookie = CookieUtils.getCookieByName(request, Constants.COOKIE_LOGIN_USER_ID);
        //用户已登陆 则直接返回
        AbstractSession session = sessionService.getSession(request);
        return isUserLogin(loginCookie, session);
    }

    /**
     * 判断用户是否正常登陆
     *
     * @param loginCookie
     * @param session
     * @return
     */
    protected boolean isUserLogin(Cookie loginCookie, AbstractSession session) {
        //判断登陆信息是否合法
        if (null == loginCookie || null == session || null == session) {
            logger.debug("----user is not login.----");
            return false;
        }
        return true;
    }

}



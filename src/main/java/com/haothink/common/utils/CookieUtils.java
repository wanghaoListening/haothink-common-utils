package com.haothink.common.utils;


import com.haothink.common.utils.encrypt.AesUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie工具类
 */
public class CookieUtils {

    public static final int ONE_DAY = 86400;
    public static final int ONE_HOUR = 3600;
    public static final int TWO_HOUR = 7200;
    public static final int HALF_HOUR = 1800;
    public static final int ONE_MINUTE = 60;

    private static final String DEFAULT_PATH = "/";

    private static int DEFAULT_EXPIRE_TIME = ONE_HOUR;


    /**
     * 设置cookie
     *
     * @param response
     * @param cookieName  cookie 存入的key
     * @param cookieValue cookie 存入的value
     */
    public static void setCookie(HttpServletResponse response, String cookieName, String cookieValue, String domain) {
        if (response == null || cookieName == null || domain == null) {
            return;
        }
        cookieValue = AesUtils.encrypt(cookieValue);
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(DEFAULT_PATH);
        cookie.setDomain(domain);
        cookie.setMaxAge(DEFAULT_EXPIRE_TIME);
        response.addCookie(cookie);
    }

    /**
     * 设置cookie
     *
     * @param response
     * @param cookieName  cookie 存入的key
     * @param cookieValue cookie 存入的value
     */
    public static void setCookieOneMinute(HttpServletResponse response, String cookieName, String cookieValue,String domain) {
        setCookie(response, cookieName, cookieValue, ONE_MINUTE,domain);
    }

    /**
     * 设置cookie
     *
     * @param response
     * @param cookieName  cookie 存入的key
     * @param cookieValue cookie 存入的value
     */
    public static void setCookieOneHour(HttpServletResponse response, String cookieName, String cookieValue,String domain) {
        setCookie(response, cookieName, cookieValue, ONE_HOUR,domain);
    }

    /**
     * 设置cookie
     *
     * @param response
     * @param cookieName  cookie 存入的key
     * @param cookieValue cookie 存入的value
     */
    public static void setCookieHalfHour(HttpServletResponse response, String cookieName, String cookieValue,String domain) {
        setCookie(response, cookieName, cookieValue, HALF_HOUR, domain);
    }

    /**
     * 设置cookie
     *
     * @param response
     * @param cookieName  cookie 存入的key
     * @param cookieValue cookie 存入的value
     */
    public static void setCookieOneDay(HttpServletResponse response, String cookieName, String cookieValue,String domain) {
        setCookie(response, cookieName, cookieValue, ONE_DAY,domain);
    }


    /**
     * 设置cookie
     *
     * @param expireSeconds 过期时间 单位秒
     * @param response
     * @param cookieName    cookie 存入的key
     * @param cookieValue   cookie 存入的value
     */
    public static void setCookie(HttpServletResponse response, String cookieName, String cookieValue, int expireSeconds, String domain) {
        if (response == null || cookieName == null || domain == null) {
            return;
        }
        cookieValue = AesUtils.encrypt(cookieValue);
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setPath(DEFAULT_PATH);
        cookie.setDomain(domain);
        cookie.setMaxAge(expireSeconds);
        response.addCookie(cookie);
    }


    /**
     * 根据cookie name 获取内容
     *
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookieValueByName(HttpServletRequest request, String cookieName) {
        if (request == null || cookieName == null) {
            return null;
        }
        String valueForName = null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Integer i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(cookieName)) {
                valueForName = AesUtils.decrypt(cookies[i].getValue());
                break;
            }
        }
        return valueForName;
    }

    /**
     * 根据cookie name 获取cookie
     * @param request
     * @param cookieName
     * @return
     */
    public static Cookie getCookieByName(HttpServletRequest request, String cookieName) {
        if (request == null || cookieName == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Integer i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * 重新设置cookie的过期时间
     * @param response
     * @param expireTime 单位:seconds
     */
    public static Cookie resetCookieExpireTime(HttpServletResponse response,Cookie cookie ,int expireTime,String domain){
        if (response == null) {
            return cookie;
        }
        cookie.setMaxAge(expireTime);
        cookie.setDomain(domain);
        cookie.setPath(DEFAULT_PATH);
        response.addCookie(cookie);
        return cookie;
    }

    /**
     * 删除cookie
     *
     * @return
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        if (request == null || response == null || cookieName == null) {
            return;
        }
        Cookie cookie = getCookieByName(request, cookieName);
        if (cookie == null) {
            return;
        }
        cookie.setValue(null);
        cookie.setPath(DEFAULT_PATH);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 删除所有cookie
     *
     * @return
     */
    public static void deleteAllCookies(HttpServletRequest request, HttpServletResponse response) {
        if (request == null || response == null) {
            return;
        }
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie == null) {
                continue;
            }
            cookie.setValue(null);
            cookie.setPath(DEFAULT_PATH);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    public int getDEFAULT_EXPIRE_TIME() {
        return DEFAULT_EXPIRE_TIME;
    }

    public void setDEFAULT_EXPIRE_TIME(int DEFAULT_EXPIRE_TIME) {
        this.DEFAULT_EXPIRE_TIME = DEFAULT_EXPIRE_TIME;
    }

    public static String getDefaultPath() {
        return DEFAULT_PATH;
    }

}

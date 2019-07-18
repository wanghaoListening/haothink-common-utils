package com.haothink.common.web.session;

import com.haothink.common.constants.Constants;
import com.haothink.common.service.Cache;
import com.haothink.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户session管理类
 * Created by wanghao on 16/4/9.
 */
public class SessionManager implements SessionService {

    protected static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    protected Cache cache;
    /**
     * 系统名称
     * 用户session在cache中的key的前缀
     */
    private String webAppName;
    /**
     * session过期时间 单位:秒
     */
    private int sessionExpireSeconds;

    @Override
    public boolean putSession(HttpServletResponse response, AbstractSession session, String domain) {
        Integer userId = session.getSessionId();

        CookieUtils.setCookie(response, Constants.COOKIE_LOGIN_USER_ID, String.valueOf(userId), getSessionExpireSeconds(), domain);

        if (session == null || userId == null) {
            logger.warn(String.format("putSession,userBean is null or userId is null"));
            return false;
        }
        try {
            String key = genKey(getWebAppName(), userId);
            logger.info("putSession,userId[{}],key[{}]", userId, key);

            return cache.put(key, session, getSessionExpireSeconds()) != null;
        } catch (Exception e) {
            logger.error("putSession to cache error!", e);
        }
        return false;
    }

    @Override
    public AbstractSession getSession(HttpServletRequest request) {
        String userId = CookieUtils.getCookieValueByName(request, Constants.COOKIE_LOGIN_USER_ID);
        if (StringUtils.isEmpty(userId)) {
            logger.debug("getSessionBean,userId is null");
            return null;
        }
        String key = genKey(getWebAppName(), userId);
        logger.debug(String.format("getSessionBean,userId[%s],key[%s]", userId, key));

        try {
            return (AbstractSession) cache.get(key);
        } catch (Exception e) {
            logger.error("getSessionBean from cache error!", e);
        }
        return null;
    }

    /**
     * 延长session过期时间
     *
     * @param userId  用户id
     * @param seconds 过期时间：秒
     */
    @Override
    public void extendExpireTime(Integer userId, Integer seconds) {
        if (null == userId) {
            logger.warn("extendExpireTime,userId is null");
            return;
        }
        try {
            String key = genKey(getWebAppName(), userId);
            cache.expireKey(key, seconds);
        } catch (Exception e) {
            logger.error("extendExpireTime error!", e);
        }
    }

    private void removeSessionBean(HttpServletRequest request) {
        String userId = CookieUtils.getCookieValueByName(request, Constants.COOKIE_LOGIN_USER_ID);
        if (StringUtils.isEmpty(userId)) {
            logger.warn(String.format("removeSessionBean,userId is null "));
            return;
        }
        String key = genKey(getWebAppName(), userId);
        Boolean result = cache.delete(key);
        if (result != null && result) {
            logger.info("delete session beans success! key: " + key);
        } else {
            logger.info("delete session beans failed ! key: " + key);
        }

    }

    /**
     * 清楚session和cookie
     *
     * @param request
     * @param response
     */
    @Override
    public void removeSessionAndCookie(HttpServletRequest request, HttpServletResponse response) {
        removeSessionBean(request);
        CookieUtils.deleteCookie(request, response, Constants.COOKIE_LOGIN_USER_ID);

        logger.info("deleteCookie success!cookieName:" + Constants.COOKIE_LOGIN_USER_ID);
    }


    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public String getWebAppName() {
        return webAppName;
    }

    public void setWebAppName(String webAppName) {
        this.webAppName = webAppName;
    }

    public int getSessionExpireSeconds() {
        return sessionExpireSeconds;
    }

    public void setSessionExpireSeconds(int sessionExpireSeconds) {
        this.sessionExpireSeconds = sessionExpireSeconds;
    }

    protected String genKey(String prefix, Integer uid) {
        return String.format("%s_%s", prefix, uid);
    }

    protected String genKey(String prefix, String uid) {
        return String.format("%s_%s", prefix, uid);
    }
}

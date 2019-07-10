package com.haothink.common.web.interceptor;


import com.haothink.common.constants.Constants;
import com.haothink.common.service.Cache;
import com.haothink.common.utils.CookieUtils;
import com.haothink.common.web.annotation.AvoidDuplicateSubmission;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * <p>
 * 防止重复提交拦截器
 * </p>
 *
 * @author: wanghao
 * @date: 2017／11／03
 */

public class AvoidDuplicateSubmissionInterceptor extends AbstractLoginInterceptor {

    /**
     * 分布式缓存服务
     */
    Cache cache;

    /**
     * 重复提交之后的错误页面
     */
    private String errUrl;
    /**
     * token过期时间 单位：秒
     */
    private static final int TOKEN_EXPIRE_TIME = 1800;
    private static final String TOKEN_NAME = "token";

    Object lock = new Object();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        AvoidDuplicateSubmission annotation = method.getAnnotation(AvoidDuplicateSubmission.class);
        if (annotation != null) {
            boolean isCreateToken = annotation.createToken();
            if (isCreateToken) {
                String tokenKey = getTokenKey(request, annotation);
                String tokenValue = String.valueOf(System.currentTimeMillis());
                Object res = cache.put(tokenKey, tokenValue, TOKEN_EXPIRE_TIME);
                request.getSession().setAttribute(TOKEN_NAME, tokenValue);
                if (res == null) {
                    logger.error("=====AvoidDuplicateSubmission put to Redis error!tokenKey:" + tokenKey);
                }
            }

            boolean isValidToken = annotation.validToken();
            //是否检查token合法
            if (isValidToken) {
                synchronized (lock) {
                    if (isRepeatSubmit(request, annotation)) {
                        try {
                            request.getSession().setAttribute("errMsg", "请不要重复提交!");
                            duplicationSubmitErrorRedirect(request, response, getErrUrl());
                        } catch (Exception e) {
                            logger.error("===== servlet redirect error!~", e);
                        }
                        return false;
                    }
                    //删除token
                    removeToken(request, annotation);
                }
            }
        }
        return true;
    }

    /**
     * @param request
     * @param avoidDuplicateSubmission
     * @return
     */
    private boolean isRepeatSubmit(HttpServletRequest request, AvoidDuplicateSubmission avoidDuplicateSubmission) {
		String tokenKey = getTokenKey(request, avoidDuplicateSubmission);
		String serverToken = cache.get(tokenKey);

		if (serverToken == null) {
			logger.info("===ServerToken is null,return duplicated");
			return true;
		}
		String clientToken = request.getParameter(TOKEN_NAME);

		if (clientToken == null) {
			logger.info("===client token is null,return duplicated");
			return true;
		}
		if (!serverToken.equals(clientToken)) {
			logger.info("key[{}],serverToken[{}]与clientToken[{}]不匹配", tokenKey, serverToken, clientToken);
			return true;
		}
		return false;
	}

    private void removeToken(HttpServletRequest request, AvoidDuplicateSubmission avoidDuplicateSubmission) {
        cache.delete(getTokenKey(request, avoidDuplicateSubmission));
    }

    private String getUserNameEncode(HttpServletRequest request) {
        Cookie cookie = CookieUtils.getCookieByName(request, Constants.COOKIE_LOGIN_USER_ID);
        if (cookie == null) {
            return null;
        }
        return cookie.getValue();
    }

    private String getTokenKey(HttpServletRequest request, AvoidDuplicateSubmission avoidDuplicateSubmission) {
        String userNameEncode = getUserNameEncode(request);
        String tokenName = avoidDuplicateSubmission.tokenName();
        return genKey(userNameEncode, tokenName);
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    private String genKey(String prefix, String suffix) {
        return prefix + "_" + suffix;
    }

    public String getErrUrl() {
        return errUrl;
    }

    public void setErrUrl(String errUrl) {
        this.errUrl = errUrl;
    }
}

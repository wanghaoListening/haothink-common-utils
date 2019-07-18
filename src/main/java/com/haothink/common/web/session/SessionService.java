package com.haothink.common.web.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by wanghao on 17/11/03.
 */
public interface SessionService {
    /**
     * 把用户session信息存储到集中式缓存中
     *
     * @param response HttpServletResponse
     * @param session 自定义的AbstractSession
     * @param domain 所在的域名 比如 zhichengcredit.com
     * @return*/
    boolean putSession(HttpServletResponse response, AbstractSession session, String domain);


    /**
     * 根据请求的sessionId获取对应session
     * */
    AbstractSession getSession(HttpServletRequest request);


    /**
     * 根据请求的sessionId删除对应session 和 cookie
     * */
    void removeSessionAndCookie(HttpServletRequest request, HttpServletResponse response);

    /**
     * 延长过期时间
     * @param userId
     * @param seconds 过期时间：秒
     * */
    void extendExpireTime(Integer userId, Integer seconds);


}

package com.haothink.common.web.session;


import java.io.Serializable;

/**
 * 抽象Session类
 * 所有使用集中式Session管理的session对象，都需要继承此对象
 */
public class AbstractSession implements Serializable {

    private static final long serialVersionUID = -3686018590732480612L;

    /**
     * 唯一标识这个session的UniqueId
     * 可以是用户UserId UserSN 根据具体业务场景决定
     */
    private Integer sessionId ;

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }
}

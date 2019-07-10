package com.haothink.common.service.impl;

import redis.clients.jedis.JedisPool;

/**
 * redis客户端 ，支持对象存储
 * Created by pengfeihu on 17/11/03.
 */
public class RedisCache extends AbstractRedisCache {

    private String serverIp;
    private int port;

    @Override
    protected void init() {
        pool = new JedisPool(getConfig(), serverIp, port, DEFAULT_CONN_TIME_OUT, password);
    }

    public RedisCache(String password, int port, String serverIp, String namespace) {
        this.password = password;
        this.port = port;
        this.serverIp = serverIp;
        this.namespace = namespace;
    }


}

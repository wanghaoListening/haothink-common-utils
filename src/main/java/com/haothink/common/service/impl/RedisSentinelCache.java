package com.haothink.common.service.impl;

import redis.clients.jedis.JedisSentinelPool;

import java.util.Set;

/**
 * redis客户端 ，支持对象存储
 * Created by wanghao on 17/11/03.
 */
public class RedisSentinelCache extends AbstractRedisCache {

    private String masterName;
    private Set<String> sentinelSet;

    @Override
    protected void init() {
        pool = new JedisSentinelPool(masterName, sentinelSet, getConfig(), DEFAULT_CONN_TIME_OUT, password);
    }

    public RedisSentinelCache(String password, String namespace, String masterName, Set<String> sentinelSet) {
        this.password = password;
        this.namespace = namespace;
        this.masterName = masterName;
        this.sentinelSet = sentinelSet;
    }

}

package com.haothink.common.service.impl;

import redis.clients.jedis.JedisSentinelPool;

import java.io.Serializable;
import java.util.Set;

/**
 * redis客户端 ，缓存在SSD上 持久化存储
 * Created by pengfeihu on 17/11/03.
 */
public class RedisDb extends AbstractRedisCache {

    private String masterName;
    private Set<String> sentinelSet;

    @Override
    protected void init() {
        pool = new JedisSentinelPool(masterName, sentinelSet, getConfig(), DEFAULT_CONN_TIME_OUT, password);
    }

    public RedisDb(String password, String namespace, String masterName, Set<String> sentinelSet) {
        this.password = password;
        this.namespace = namespace;
        this.masterName = masterName;
        this.sentinelSet = sentinelSet;
    }

    public <T extends Serializable> T put(String key, T obj) {
        return put(key, obj, NEVER_EXPIRED, namespace);
    }


    public <T extends Serializable> T put(String key, T obj,String namespace) {
        return put(key, obj, NEVER_EXPIRED, namespace);
    }

}

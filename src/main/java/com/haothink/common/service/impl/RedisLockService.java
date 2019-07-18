package com.haothink.common.service.impl;

import com.haothink.common.service.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * 基于redis的分布式锁服务
 * Created by wanghao on 16/5/18.
 */
public class RedisLockService implements LockService {

    private static final Logger logger = LoggerFactory.getLogger(RedisLockService.class);

    /**
     * 默认占用锁的时间 单位：秒
     */
    private static final int DEFAULT_KEEP_LOCK_SECONDS = 300;

    /**
     * 重试间隔时间 单位：ms
     */
    private static final int TRY_LOCK_SLEEP_TIME = 1000;

    private static final String namespace = "LOCK_";

    @Autowired
    RedisSentinelCache cache;

    @Override
    public boolean tryLock(String key) {
        return tryLock(key, 0);
    }

    /**
     * 锁在给定的等待时间内空闲，则获取锁成功 返回true， 否则返回false
     *
     * @param waitLockSeconds 单位:秒 获取锁等待的时间，在等待时间内会一直重试，如果超时则返回未获取
     * @return
     */
    @Override
    public boolean tryLock(String key, int waitLockSeconds) {
        return tryLock(key, waitLockSeconds, DEFAULT_KEEP_LOCK_SECONDS);
    }

    @Override
    public boolean tryLock(String key, int waitLockSeconds, int keepLockSeconds) {
        try {
            //系统计时器的当前值，以毫微秒为单位。
            long startTime = System.currentTimeMillis();
            long waitLockTimeMills = waitLockSeconds * 1000;

            key = addPrefix(key);
            do {
                logger.debug("try lock key: " + key);
                //将 key 的值设为 value 1成功  0失败
                boolean isGetLock = cache.putNx(key, key, keepLockSeconds);

                if (isGetLock) {
                    //设置过期时间
                    logger.debug("get lock, key: " + key + " , expire in " + keepLockSeconds + " seconds.");
                    //成功获取锁，返回true
                    return Boolean.TRUE;
                }
                // 存在锁,循环等待锁
                logger.debug("key: " + key + " locked by another business：" + key);

                if (waitLockTimeMills <= 0) {
                    //没有设置超时时间，直接退出等待
                    break;
                }
                Thread.sleep(TRY_LOCK_SLEEP_TIME);
            } while ((System.currentTimeMillis() - startTime) < waitLockTimeMills);
        } catch (JedisConnectionException je) {
            logger.error("redis 连接异常!", je);
        } catch (Exception e) {
            logger.error(String.format("获取分布式锁异常!key[%s]value[%s]", key, cache.get(key)), e);
        }
        return Boolean.FALSE;
    }


    @Override
    public boolean unLock(String key) {
        key = addPrefix(key);
        return cache.expireKey(key, 0);
    }

    private String addPrefix(String key) {
        return namespace + key;
    }

    public RedisSentinelCache getCache() {
        return cache;
    }

    public void setCache(RedisSentinelCache cache) {
        this.cache = cache;
    }
}

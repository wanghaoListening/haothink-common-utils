package com.haothink.common.service;

import java.io.Serializable;

/**
 * Created by wanghao on 17/11/03.
 */
public interface Cache {

    /**
     * Get the object by key, object can be a String,
     * String list, String set, String hash.
     * @param key the cache key
     * @param <T> returned object
     * @return
     */
    <T extends Serializable> T get(String key) ;

    <T extends Serializable> T get(String key, String nameSpace) ;

    /**
     * put the entire Object to redis, if redis exists the same key,
     * will delete it, be careful if use this method, this is a replace method.
     * 这个方法将用传入的key和obj, 替换掉缓存上对应的key(针对list, set, hashset),
     *
     * @param key the key in cache
     * @param obj return obj if success, return null if failed.
     * @param expireSeconds seconds  -1:永不过期
     * @param <T>
     * @return
     */
    <T extends Serializable> T put(String key, T obj, int expireSeconds) ;

    <T extends Serializable> T put(String key, T obj, int expireSeconds, String nameSpace) ;

    /**
     * 删除掉cache上对应key的内容, 小心使用, 如果只想删掉对应列表中对应key
     * 的某一项, 请使用remove方法.
     * @param key key in cache
     * @return success return true
     */
    boolean delete(String key);

    boolean delete(String key, String nameSpace);

    /**
     * 设置某个key的过期时间
     *
     * @param key
     * @param seconds
     * @return true:设置成功 false:失败
     */
    boolean expireKey(String key, int seconds);

    boolean expireKey(String key, int seconds, String nameSpace);


    /**
     * 初始化原子被加数
     * 注意：由于redis的限制，原子操作数必须使用putAtomic方法放入，才能执行原子方法操作
     * @param key
     * @param num
     * @return
     */
    Long putAtomic(String key, Long num, int expireSeconds);
    /**
     * 获取原子数
     * @param key
     * @return
     */
    Long getAtomic(String key);
    /**
     * 获取原子数
     * @param key
     * @param nameSpace
     * @return
     */
    Long getAtomic(String key, String nameSpace);


    /**
     * 原子加法
     * 注意：由于redis的限制，原子操作数必须使用putAtomic方法放入，才能执行原子方法操作
     * @param key
     * @param count
     * @return
     */
    Long incr(String key, int count);

    /**
     * 原子加法
     * 注意：由于redis的限制，原子操作数必须使用putAtomic方法放入，才能执行原子方法操作
     * @param key
     * @param count
     * @param nameSpace
     * @return
     */
    Long incr(String key, int count, String nameSpace);

    /**
     * 原子减法
     * 注意：由于redis的限制，原子操作数必须使用putAtomic方法放入，才能执行原子方法操作
     *
     * @param key
     * @param count
     * @return
     */
    Long decr(String key, int count);

    /**
     * 原子减法
     * 注意：由于redis的限制，原子操作数必须使用putAtomic方法放入，才能执行原子方法操作
     * @param key
     * @param count
     * @param nameSpace
     * @return
     */
    Long decr(String key, int count, String nameSpace);

}

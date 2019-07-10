package com.haothink.common.service;

/**
 * 分布式锁服务
 * Created by pengfeihu on 16/5/18.
 */
public interface LockService {

    /**
     * 获取锁,如果锁可用立即返回true，否则返回false
     *
     * @param key
     * @return
     */
    boolean tryLock(String key);

    /**
     * 锁在给定的等待时间内，获取锁成功 返回true， 否则返回false
     *
     * @param key
     * @return
     */
    boolean tryLock(String key, int waitLockSeconds);


    /**
     * 锁在给定的等待时间内，获取锁成功 返回true， 否则返回false
     *
     * @param key
     * @return
     */
    boolean tryLock(String key, int waitLockSeconds, int keepLockSeconds);

    /**
     * 释放锁
     *
     * @param key
     */
    boolean unLock(String key);




}
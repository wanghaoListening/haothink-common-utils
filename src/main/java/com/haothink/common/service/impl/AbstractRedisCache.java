package com.haothink.common.service.impl;

import com.haothink.common.service.Cache;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

import java.io.Serializable;

/**
 * Created by wanghao 2017/11/27
 *
 * @author wanghao
 */
public abstract class AbstractRedisCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(AbstractRedisCache.class);

    protected String password;
    protected Pool<Jedis> pool;
    protected String namespace = "default";
    protected int maxTotal = 3000;
    protected int maxIdle = 10;
    /**
     * 最大等待超时 单位：ms
     */
    protected int maxWaitMillis = 60000;
    /**
     * 最大连接超时 单位：ms
     */
    protected static final int DEFAULT_CONN_TIME_OUT = 3000;
    /**
     * 数据存储用不过期
     */
    protected static final int NEVER_EXPIRED = -1;


    abstract void init();

    protected JedisPoolConfig getConfig() {
        // 建立连接池配置参数
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(maxTotal);
        // 设置最大阻塞时间，记住是毫秒数milliseconds
        config.setMaxWaitMillis(maxWaitMillis);
        // 设置空闲连接
        config.setMaxIdle(maxIdle);
        // jedis实例是否可用
        config.setTestOnBorrow(Boolean.TRUE);
        config.setTestOnReturn(Boolean.FALSE);
        //在空闲时检查有效性, 默认false
        config.setTestWhileIdle(true);
        //逐出扫描的时间间隔(毫秒) 如果为负数, 则不运行逐出线程, 默认 - 1
        config.setTimeBetweenEvictionRunsMillis(60000);
        //对象空闲多久后逐出, 当空闲时间 > 该值 且 空闲连接>最大空闲数 时直接逐出, 不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
        config.setSoftMinEvictableIdleTimeMillis(1800000);
        //每次逐出检查时 逐出的最大数目 如果为负数就是: idleObjects.size / abs(n), 默认3
        config.setNumTestsPerEvictionRun(3);
        return config;
    }

    @Override
    public <T extends Serializable> T get(String key) {
        return get(key, getNamespace());
    }

    @Override
    public Long getAtomic(String key) {
        return getAtomic(key,getNamespace());

    }

    @Override
    public Long getAtomic(String key, String nameSpace) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (null == jedis || !jedis.exists(key)) {
                return null;
            }
            return Long.valueOf(jedis.get(key));
        } catch (Exception e) {
            log.error(String.format("redis get key[%s] error!", key), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    @Override
    public <T extends Serializable> T get(String key, String nameSpace) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (null == jedis || !jedis.exists(key)) {
                return null;
            }
            byte[] value = jedis.get(key.getBytes());
            return (T) SerializationUtils.deserialize(value);
        } catch (Exception e) {
            log.error(String.format("redis get key[%s] error!", key), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    @Override
    public <T extends Serializable> T put(String key, T obj, int expireSeconds) {
        return put(key, obj, expireSeconds, getNamespace());
    }

    @Override
    public Long putAtomic(String key, Long num, int expireSeconds) {
        return put(key, num, expireSeconds, getNamespace(), true);
    }

    @Override
    public <T extends Serializable> T put(String key, T obj, int expireSeconds, String nameSpace) {
        return put(key, obj, expireSeconds, getNamespace(), false);
    }

    /**
     * @param key                key
     * @param obj                value值
     * @param expireSeconds      过期时间
     * @param nameSpace          命名空间
     * @param isStringSerialized 是否使用字符串序列化方式
     * @param <T>
     * @return
     */
    public <T extends Serializable> T put(String key, T obj, int expireSeconds, String nameSpace, boolean isStringSerialized) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (jedis.exists(key)) {
                _delete(key, nameSpace, jedis);
            }
            //原子自增操作 必须使用String序列化方式存储
            if (isStringSerialized) {
                jedis.set(key, String.valueOf(obj));
            } else {
                jedis.set(key.getBytes(), SerializationUtils.serialize(obj));
            }
            //-1 表示不设置超时
            if (expireSeconds != -1) {
                _expireKey(key, expireSeconds);
            }
            return obj;
        } catch (Exception e) {
            log.error(String.format("redis put key[%s] error!", key), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    /**
     * 分布式锁使用 同一时间只能put唯一值
     *
     * @param key
     * @param obj
     * @param expireSeconds seconds
     * @param <T>
     * @return
     */
    public <T extends Serializable> boolean putNx(String key, T obj, int expireSeconds) {
        if (key == null || obj == null) {
            return false;
        }
        key = genKey(key, this.getNamespace());
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            Long res = jedis.setnx(key.getBytes(), SerializationUtils.serialize(obj));
            if (res == 1) {
                _expireKey(key, expireSeconds, jedis);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(String.format("redis putNx key[%s] error!", key), e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    @Override
    public boolean delete(String key) {
        return delete(key, getNamespace());
    }

    @Override
    public boolean delete(String key, String nameSpace) {
        if (key == null) {
            return false;
        }
        key = genKey(key, nameSpace);
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.del(key.getBytes()) > 0;
        } catch (Exception e) {
            log.error(String.format("redis delete key[%s] error!", key), e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private boolean _delete(String key, String nameSpace, Jedis jedis) {
        if (key == null) {
            return false;
        }
        key = genKey(key, nameSpace);
        try {
            return jedis.del(key.getBytes()) > 0;
        } catch (Exception e) {
            log.error(String.format("redis delete key[%s] error!", key), e);
            return false;
        }
    }


    private boolean _expireKey(String key, int seconds, Jedis jedis) {
        if (key == null) {
            return false;
        }
        try {
            if (jedis.exists(key)) {
                return jedis.expire(key.getBytes(), seconds) == 1;
            }
            return false;
        } catch (Exception e) {
            log.error(String.format("redis expireKey key[%s] error!", key), e);
            return false;
        }
    }

    private boolean _expireKey(String key, int seconds) {
        if (key == null) {
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (jedis.exists(key)) {
                return jedis.expire(key.getBytes(), seconds) == 1;
            }
            return false;
        } catch (Exception e) {
            log.error(String.format("redis expireKey key[%s] error!", key), e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public boolean expireKey(String key, int seconds) {
        return expireKey(key, seconds, getNamespace());
    }

    @Override
    public boolean expireKey(String key, int seconds, String nameSpace) {
        if (key == null) {
            return false;
        }
        key = genKey(key, nameSpace);
        return _expireKey(key, seconds);
    }

    @Override
    public Long incr(String key, int count) {
        return incr(key, count, getNamespace());
    }

    @Override
    public Long incr(String key, int count, String nameSpace) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (null == jedis || !jedis.exists(key)) {
                return null;
            }
            return jedis.incrBy(key.getBytes(), count);
        } catch (Exception e) {
            log.error(String.format("redis incr key[%s]count[%s] error!", key, count), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public Long decr(String key, int count) {
        return decr(key, count, getNamespace());
    }

    @Override
    public Long decr(String key, int count, String nameSpace) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if (null == jedis || !jedis.exists(key)) {
                return null;
            }
            return jedis.decrBy(key.getBytes(), count);
        } catch (Exception e) {
            log.error(String.format("redis decr key[%s]count[%s] error!", key, count), e);
            return null;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * 支持星探特殊需求使用
     * @return
     */
    public Jedis getInstance() {
        return pool.getResource();
    }

    protected String genKey(String key) {
        return genKey(key, namespace);
    }

    protected String genKey(String key, String namespace) {
        return namespace + "_" + key;
    }


}

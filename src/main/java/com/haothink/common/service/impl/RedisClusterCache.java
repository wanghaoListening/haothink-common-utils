package com.haothink.common.service.impl;

import com.haothink.common.service.Cache;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by wanghao 2017/11/18
 *
 * @author pengfeihu
 */
public class RedisClusterCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(RedisClusterCache.class);

    private String hostAndPorts;//ip地址和端口的集合
    private int connectionTimeout;//连接超时时间
    private int socketTimeout;//读取超时间
    private int maxAttempts;//最大尝试次数
    private String password;//密码
    private String namespace = "default";
    private JedisCluster cluster;

    private int maxTotal = 3000;//pool中对象最多能有多少
    private int maxIdle = 10;//pool中最多能保留多少个空闲对象
    private int minIdle = 0;//pool中最少有多少个空闲对象
    private static final String CHAR_SPLIT = ",";
    private static final String IP_PORT_SPLIT = ":";

    public RedisClusterCache(String hostAndPorts, int connectionTimeout, int socketTimeout, int maxAttempts, String password, String namespace, int maxTotal, int maxIdle, int minIdle) {
        this.hostAndPorts = hostAndPorts;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        this.maxAttempts = maxAttempts;
        this.password = password;
        this.namespace = namespace;
        this.maxTotal = maxTotal;
        this.maxIdle = maxIdle;
        this.minIdle = minIdle;
    }

    protected void init() throws Exception {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        Set<HostAndPort> hostAndPortSet = parseHostAndPort();
        poolConfig.setMaxIdle(this.maxIdle);
        poolConfig.setMaxTotal(this.maxTotal);
        poolConfig.setMinIdle(this.minIdle);
        cluster = new JedisCluster(hostAndPortSet, connectionTimeout, socketTimeout, maxAttempts, password, poolConfig);
    }

    /**
     * 通过得到redis的配置文件 分割多个
     *
     * @return hostAndPortSet
     */
    private Set<HostAndPort> parseHostAndPort() {
        Set<HostAndPort> hostAndPortSet = new HashSet<>();
        try {
            String[] addressArray = StringUtils.split(this.hostAndPorts, CHAR_SPLIT);
            for (String address : addressArray) {
                String serverIp = address.split(IP_PORT_SPLIT)[0];
                int port = Integer.parseInt(address.split(IP_PORT_SPLIT)[1]);
                HostAndPort hap = new HostAndPort(serverIp, port);
                hostAndPortSet.add(hap);
            }
        } catch (Exception e) {
            log.error("解析 jedis 配置文件失败 得到的redis地址属性为:{}", this.hostAndPorts, e);
        }
        return hostAndPortSet;
    }


    @Override
    public <T extends Serializable> T get(String key) {
        return get(key, namespace);
    }

    @Override
    public <T extends Serializable> T get(String key, String nameSpace) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        try {
            if (null == cluster || !cluster.exists(key)) {
                return null;
            }
            byte[] value = cluster.get(key.getBytes());
            return (T) SerializationUtils.deserialize(value);
        } catch (Exception e) {
            log.error(String.format("redis get key[%s] error!", key), e);
            return null;
        }
    }

    @Override
    public <T extends Serializable> T put(String key, T obj, int expireSeconds) {
        return put(key, obj, expireSeconds, namespace);
    }

    @Override
    public <T extends Serializable> T put(String key, T obj, int expireSeconds, String nameSpace) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        try {
            if (cluster.exists(key)) {
                _delete(key, nameSpace);
            }
            cluster.set(key.getBytes(), SerializationUtils.serialize(obj));
            //-1 表示不设置超时
            if (expireSeconds != -1) {
                _expireKey(key, expireSeconds);
            }
            return obj;
        } catch (Exception e) {
            log.error(String.format("redis put key[%s] error!", key), e);
            return null;
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
        try {
            Long res = cluster.setnx(key.getBytes(), SerializationUtils.serialize(obj));
            if (res == 1) {
                _expireKey(key, expireSeconds);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(String.format("redis putNx key[%s] error!", key), e);
            return false;
        }
    }


    @Override
    public boolean delete(String key) {
        return delete(key, namespace);
    }

    @Override
    public boolean delete(String key, String nameSpace) {
        if (key == null) {
            return false;
        }
        key = genKey(key, nameSpace);
        try {
            return cluster.del(key.getBytes()) > 0;
        } catch (Exception e) {
            log.error(String.format("redis delete key[%s] error!", key), e);
            return false;
        }
    }

    private boolean _delete(String key, String nameSpace) {
        if (key == null) {
            return false;
        }
        key = genKey(key, nameSpace);
        try {
            return cluster.del(key.getBytes()) > 0;
        } catch (Exception e) {
            log.error(String.format("redis delete key[%s] error!", key), e);
            return false;
        }
    }


    private boolean _expireKey(String key, int seconds) {
        if (key == null) {
            return false;
        }
        try {
            if (cluster.exists(key)) {
                return cluster.expire(key.getBytes(), seconds) == 1;
            }
            return false;
        } catch (Exception e) {
            log.error(String.format("redis expireKey key[%s] error!", key), e);
            return false;
        }
    }

    @Override
    public boolean expireKey(String key, int seconds) {
        return expireKey(key, seconds, namespace);
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
    public Long putAtomic(String key, Long num, int expireSeconds) {
        //TODO ClusterCache需要整体优化
        return null;
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
        try {
            if (null == cluster || !cluster.exists(key)) {
                return null;
            }
            return Long.valueOf(cluster.get(key));
        } catch (Exception e) {
            log.error(String.format("redis get key[%s] error!", key), e);
            return null;
        }
    }


    @Override
    public Long incr(String key, int count) {
        return incr(key, count, namespace);
    }

    @Override
    public Long incr(String key, int count, String nameSpace) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        try {
            if (null == cluster || !cluster.exists(key)) {
                return null;
            }
            return cluster.incrBy(key.getBytes(), count);
        } catch (Exception e) {
            log.error(String.format("redis incr key[%s]count[%s] error!", key, count), e);
            return null;
        }
    }

    @Override
    public Long decr(String key, int count) {
        return decr(key, count, namespace);
    }

    @Override
    public Long decr(String key, int count, String nameSpace) {
        if (key == null) {
            return null;
        }
        key = genKey(key, nameSpace);
        try {
            if (null == cluster || !cluster.exists(key)) {
                return null;
            }
            return cluster.decrBy(key.getBytes(), count);
        } catch (Exception e) {
            log.error(String.format("redis decr key[%s]count[%s] error!", key, count), e);
            return null;
        }
    }

    private String genKey(String key) {
        return genKey(key, namespace);
    }

    private String genKey(String key, String namespace) {
        return namespace + "_" + key;
    }

    public String getHostAndPorts() {
        return hostAndPorts;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public String getPassword() {
        return password;
    }

    public String getNamespace() {
        return namespace;
    }

    public JedisCluster getCluster() {
        return cluster;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }
}


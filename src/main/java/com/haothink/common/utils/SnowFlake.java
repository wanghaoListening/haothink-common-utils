package com.haothink.common.utils;

/**
 * twitter的snowflake算法 -- java实现
 * <p>
 * 最大支持2个数据中心，共128个节点 每个结点每秒1024个全局唯一性id生成
 *
 * @author wanghao
 * @date 2018/4/14
 */
public class SnowFlake {

    /**
     * 起始的时间戳
     */
    private final static long START_STMP = 1480166465631L;

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 10; //序列号占用的位数
    private final static long MACHINE_BIT = 6;   //机器标识占用的位数
    private final static long DATACENTER_BIT = 1;//数据中心占用的位数

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * 支持的最大节点数
     */
    private final static long MAX_NODE_NUM = 127;


    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;


    /**
     * 序列号
     */
    private static long sequence = 0L;
    /**
     * 上一次时间戳
     */
    private static long lastStmp = -1L;
    /**
     * 上一次生成的ID
     */
    private static long lastId = -1L;


    /**
     * 获得全局唯一Id
     * machineId 在 [0-127]之间
     *
     * @param machineId
     * @return
     */
    public static long getUniqueId(long machineId) {
        //最大支持128个节点
        if (machineId > MAX_NODE_NUM || machineId < 0) {
            throw new IllegalArgumentException(String.format("machineId can't be greater than %s or less than 0",MAX_NODE_NUM));
        }
        long dataCenterId;

        //machineId 64-127
        if (machineId > MAX_MACHINE_NUM) {
            machineId -= 64;
            dataCenterId = 1;
            //machineId 0-63
        } else {
            dataCenterId = 0;
        }
        return getUniqueId(dataCenterId, machineId);
    }

    /**
     * 获得全局唯一Id
     * machineId    必须在 [0-63] 之间
     * dataCenterId 必须在 [0-1] 之间
     *
     * @param dataCenterId 数据中心Id
     * @param machineId    机器标识Id
     * @return
     */
    private static long getUniqueId(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATACENTER_NUM || dataCenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        return nextId(dataCenterId, machineId);
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    private static synchronized long nextId(long dataCenterId, long machineId) {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        long currentId =  (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | dataCenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;                             //序列号部分

        if (currentId > lastId) {
            lastId = currentId;
        } else {
            currentId = ++lastId;
        }
        return currentId;
    }

    private static long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private static long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {

        for (int i = 0; i < 1024; i++) {
            long id = SnowFlake.getUniqueId( 64);
            String str = String.valueOf(id);
            System.out.println(id);
            System.out.println(str.length());

        }
        System.out.println("down");


    }
}
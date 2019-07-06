package com.haothink.common.mq;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public abstract class AbstractMqConsumer<T> extends DefaultMQPushConsumer {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractMqConsumer.class);


    @PostConstruct
    public void setMessageListener() {
        AbstractMessageListener abstractMessageListener = new AbstractMessageListener<T>() {

            @Override
            public void consumeMessage(T t) {
                dealMessage(t);
            }
        };
        super.setMessageListener(abstractMessageListener);
    }

    /**
     * 处理消息
     * @param t
     */
    public abstract void dealMessage(T t);


}

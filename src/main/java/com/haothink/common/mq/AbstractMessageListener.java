package com.haothink.common.mq;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 封装接受消息的listener
 */

public abstract class AbstractMessageListener<T> implements MessageListenerConcurrently {

    private static Logger logger = LoggerFactory.getLogger(AbstractMessageListener.class);


    /**
     * @param list
     * @param consumeConcurrentlyContext
     * @return
     */
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

        for (MessageExt messageExt : list) {
            try {
                logger.info("consumeMessage?get message#messageExt={}", messageExt);

                T t = SerializationUtils.deserialize(messageExt.getBody());

                consumeMessage(t);

            } catch (Exception e) {
                logger.info("#CallBackOrgMessageListener.consumeMessage# An exception occurred while receiving the message,messageExt={}", messageExt, e);
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }


    /**
     * 真正的消费处理
     *
     * @param t
     */
    public abstract void consumeMessage(T t);
}

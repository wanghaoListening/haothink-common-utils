package com.haothink.common.mq;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * 给机构推送
 */
public class MQSendProducer extends DefaultMQProducer {
    private static final Logger logger = LoggerFactory.getLogger(MQSendProducer.class);

    private String topicPrefix;


    private static final MyMessageQueueSelector myMessageQueueSelector = new MyMessageQueueSelector();

    /**
     * 有序发送
     * @param id  用于有序标记有序的统一id 必须传输id，表示id这一批次的是有序的
     * 慎用，只会占用一个queue
     */
    public boolean sendOrderly(String topic,String tag,Integer id, Object message)  {

        if(StringUtils.isBlank(topic) || StringUtils.isBlank(tag) || id == null) {
            logger.error("sendOrderly lack parameter#topic={},tag={},id={}",topic,tag,id);
        }

        Message msg = new Message(topicPrefix + topic, tag, SerializationUtils.serialize((Serializable) message));

        try {
            sendOrderly(id,msg);
        } catch (Exception e) {
            logger.error("send error#topic={},tag={},id={},param={}",topic,tag,id,message,e);
            return false;
        }
        return true;
    }

    /**
     * 无序发送
     * 能充分占用所有queue
     */
    public boolean sendNotOrderly(String topic,String tag, Object message)  {

        if(StringUtils.isBlank(topic) || StringUtils.isBlank(tag) ) {
            logger.error("sendOrderly lack parameter#topic={},tag={}",topic,tag);
        }

        Message msg = new Message(topicPrefix + topic, tag, SerializationUtils.serialize((Serializable) message));

        try {
            sendNotOrderly(msg);
        } catch (Exception e) {
            logger.error("send error#topic={},tag={},param={}",topic,tag,message,e);
            return false;
        }
        return true;
    }

    private void sendNotOrderly(Message msg) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        this.send(msg);
    }

    private void sendOrderly(Integer id,Message msg) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
         this.send(msg,myMessageQueueSelector,id);
    }

    static class MyMessageQueueSelector implements MessageQueueSelector {

        @Override
        public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
            Integer id = (Integer) arg;
            int index = id % mqs.size();
            return mqs.get(index);
        }
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }
}

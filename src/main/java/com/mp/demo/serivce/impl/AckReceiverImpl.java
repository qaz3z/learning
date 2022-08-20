package com.mp.demo.serivce.impl;

import cn.hutool.Hutool;
import cn.hutool.core.date.DateUtil;
import com.mp.demo.config.RabbitMqConfig;
import com.mp.demo.serivce.ConsumerReceiverService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-19
 * @since JDK 1.8
 */
@Service
@Slf4j
public class AckReceiverImpl implements ChannelAwareMessageListener {

    /**
     * 用户消息接收类需要延迟发布的时间
     */
    @Autowired
    private ConsumerReceiverService consumerReceiverService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        //时间格式
        String now = DateUtil.now();
        log.info("消息接收成功，接收时间：[{}]",now);

        String queueName = message.getMessageProperties().getConsumerQueue();
        switch (queueName){
            case RabbitMqConfig.DELAY_QUEUE:
            case RabbitMqConfig.DIRECT_QUEUE:
                consumerReceiverService.receiverJson(message, channel);
                break;
            default:
        }
    }
}
package com.mp.demo.serivce;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.List;

/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-19
 * @since JDK 1.8
 */
public interface ConsumerReceiverService {
    /****
     *
     * @param message 消息
     * @param channel  渠道
     * @throws IOException
     */
    void receiverJson(Message message, Channel channel) throws IOException;
}
package com.mp.demo.config;

import cn.hutool.core.util.IdUtil;
import com.mp.demo.serivce.impl.AckReceiverImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-17
 * @since JDK 1.8
 */
@Configuration
@Slf4j
public class RabbitMqConfig {

    //Direct队列名称
    public static final String DIRECT_QUEUE = "direct_queue";
    //交换器名称
    public static final String DIRECT_EXCHANGE = "direct_exchange";
    //路由键
    public static final String DIRECT_ROUTING_KEY = "direct_routing_key";
    //延时队列名称
    public static final String DELAY_QUEUE = "delay_queue";
    //交换器名称
    public static final String DELAY_EXCHANGE = "delay_exchange";
    //路由键
    public static final String DELAY_ROUTING_KEY = "delay_routing_key";


    /**
     * 消息接收确认处理类
     */
    @Autowired
    private AckReceiverImpl ackReceiver;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Bean
    public RabbitTemplate createRabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        // 设置Json转换器
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // 设置开启Mandatory,才能触发回调函数,无论消息推送结果怎么样都强制调用回调函数
        rabbitTemplate.setMandatory(true);

        // 确认消息送到交换机(Exchange)回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            log.info(" 确认消息送到交换机(Exchange)结果：");
            log.info("相关数据：[{}]", correlationData);
            log.info("ack[{}]", ack);
            if (ack) {
                log.info("发送成功");

            } else {
                log.info("失败成功");
            }
            log.info("错误原因：[{}]", cause);
        });

        // 确认消息送到队列(Queue)回调
        rabbitTemplate.setReturnsCallback(returnedMessage -> {
            log.info("=====确认消息送到队列(Queue)结果：");
            log.info("发生消息：[{}]", returnedMessage.getMessage());
            log.info("回应码：[{}]", returnedMessage.getReplyCode());
            log.info("回应信息：[{}]", returnedMessage.getReplyText());
            log.info("交换机：[{}]", returnedMessage.getExchange());
            log.info("路由键：[{}]", returnedMessage.getRoutingKey());
        });
        return rabbitTemplate;
    }

    /**
     * Json转换器
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Direct交换器
     */
    @Bean
    public DirectExchange directExchange() {
        /**
         * 创建交换器，参数说明：
         * String name：交换器名称
         * boolean durable：设置是否持久化，默认是 false。durable 设置为 true 表示持久化，反之是非持久化。
         * 持久化可以将交换器存盘，在服务器重启的时候不会丢失相关信息。
         * boolean autoDelete：设置是否自动删除，为 true 则设置队列为自动删除，
         */
        return new DirectExchange(DIRECT_EXCHANGE, true, false);
    }

    /**
     * 队列
     */
    @Bean
    public Queue directQueue() {
        /**
         * 创建队列，参数说明：
         * String name：队列名称。
         * boolean durable：设置是否持久化，默认是 false。durable 设置为 true 表示持久化，反之是非持久化。
         * 持久化的队列会存盘，在服务器重启的时候不会丢失相关信息。
         * boolean exclusive：设置是否排他，默认也是 false。为 true 则设置队列为排他。
         * boolean autoDelete：设置是否自动删除，为 true 则设置队列为自动删除，
         * 当没有生产者或者消费者使用此队列，该队列会自动删除。
         * Map<String, Object> arguments：设置队列的其他一些参数。
         */
        return new Queue(DIRECT_QUEUE, true, false, false, null);
    }

    /**
     * 绑定
     */
    @Bean
    Binding directBinding(DirectExchange directExchange, Queue directQueue) {
        // 将队列和交换机绑定, 并设置用于匹配键：routingKey路由键
        return BindingBuilder.bind(directQueue).to(directExchange).with(DIRECT_ROUTING_KEY);
    }

    /******************************延时队列******************************/

    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>(1);
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAY_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue delayQueue() {
        Queue queue = new Queue(DELAY_QUEUE, true);
        return queue;
    }

    @Bean
    public Binding delaybinding(Queue delayQueue, CustomExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with(DELAY_ROUTING_KEY).noargs();
    }

    /**
     * 客户端配置
         * 配置手动确认消息、消息接收确认
     */
    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer() {
        //消费者数量，默认10
        int DEFAULT_CONCURRENT = 10;

        //每个消费者获取最大投递数量 默认50
        int DEFAULT_PREFETCH_COUNT = 50;

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setConcurrentConsumers(DEFAULT_CONCURRENT);
        container.setMaxConcurrentConsumers(DEFAULT_PREFETCH_COUNT);

        // RabbitMQ默认是自动确认，这里改为手动确认消息
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        //添加队列，可添加多个队列
        container.addQueues(new Queue(DIRECT_QUEUE, true));
        container.addQueues(new Queue(DELAY_QUEUE, true));
        IdUtil.getSnowflakeNextId();
        //设置消息处理类
        container.setMessageListener(ackReceiver);

        return container;
    }

}


package com.mp.demo.serivce.impl;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.demo.constant.RedisKey;
import com.mp.demo.entity.MsgVO;
import com.mp.demo.serivce.ConsumerReceiverService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-19
 * @since JDK 1.8
 */
@Service
@Slf4j
public class ConsumerReceiverImpl implements ConsumerReceiverService {
    @Resource
    private RedisTemplate redisTemplate;

    /****
     *
     * @param message 消息
     * @param channel  渠道
     * @throws IOException
     */
    @Override
    public void receiverJson(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 将JSON格式数据转换为实体对象
            byte[] body = message.getBody();
            MsgVO msgVO = JSON.parseObject(body, MsgVO.class);
            // 是否校验重复字段参数 目前之前pushTime,content,msgId 如果为空这不进行校验 按照顺序凭借
            String checkField = msgVO.getCheckField();

            if (StringUtils.isNotBlank(checkField)){
                String[] field = checkField.split(",");
                StringBuilder checkContent = new StringBuilder();
                for (String property : field) {
                    String fieldValue;
                    if (StringUtils.startsWith(property,"info")){
                        String propertyEnd = StringUtils.substringAfterLast(property, ".");
                        Object name = ReflectUtil.getFieldValue(msgVO, "info");
                        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(name);
                        fieldValue = jsonObject.getString(propertyEnd);
                    }else {
                        fieldValue = String.valueOf(ReflectUtil.getFieldValue(msgVO, property));
                    }
                    if (StringUtils.isNotBlank(fieldValue)){
                        checkContent.append(fieldValue);
                    }
                }
                String msg = SecureUtil.md5(checkContent.toString());
                String key = RedisKey.PRE_MSG.concat(msg);
                Boolean isExist = redisTemplate.hasKey(key);
                if (isExist) {
                    log.info("已经发送过的重复消息");
                } else {
                    redisTemplate.opsForValue().set(key, "1", 20, TimeUnit.MINUTES);
                    log.info("第一次发送的消息");
                }
            }else{
                log.info("收到发送的消息不校验重复");

            }
            log.info("消费收到JSON格式消息：");
            log.info("[{}]", JSON.toJSONString(msgVO));
        } catch (Exception e) {
            channel.basicReject(deliveryTag, false);
            e.printStackTrace();
        } finally {
            /**
             * 确认消息，参数说明：
             * long deliveryTag：唯一标识 ID。
             * boolean multiple：是否批处理，当该参数为 true 时，
             * 则可以一次性确认 deliveryTag 小于等于传入值的所有消息。
             */
            channel.basicAck(deliveryTag, true);
        }
    }
}
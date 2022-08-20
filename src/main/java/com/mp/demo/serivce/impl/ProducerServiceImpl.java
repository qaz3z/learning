package com.mp.demo.serivce.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mp.demo.config.RabbitMqConfig;
import com.mp.demo.config.SnowflakeConfig;
import com.mp.demo.entity.MsgDTO;
import com.mp.demo.entity.MsgVO;
import com.mp.demo.serivce.ProducerService;
import com.mp.demo.serivce.indecent.IndecentService;
import com.mp.demo.serivce.word.SensitiveWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * <b>功能描述:</b><br>
 * @author qaz
 * @version 1.0.0
 * @Note <b>创建时间:</b> 2022-08-19
 * @since JDK 1.8
 */
@Service
@Slf4j
public class ProducerServiceImpl implements ProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private IndecentService indecentService;
    /**
     * 发送json格式数据
     *
     * @param msgVO
     */
    @Override
    public HashMap<String, Object>  sendTestJson(MsgVO msgVO) {
        HashMap<String, Object> result = new HashMap<>(8);
        Boolean checkWord = msgVO.getCheckWord();
        if (checkWord){
            HashMap<String, Object> info = (HashMap<String, Object>) msgVO.getInfo();
            String content = (String) info.get("content");
            HashMap<String, Object> resultWord = indecentService.getSensitiveWordsByDFA(content, true, 1);
            if (MapUtil.isNotEmpty(resultWord)){
                return resultWord;
            }
        }
        rabbitTemplate.convertAndSend(RabbitMqConfig.DIRECT_EXCHANGE, RabbitMqConfig.DIRECT_ROUTING_KEY, msgVO);
        log.info("json格式的数据发送成功 发送时间为[{}]", DateUtil.now());
        result.put("code",200);
        result.put("msg","发送成功！");
        return result;
    }

    /**
     * 延时发送map格式数据
     *
     * @param msgVO
     */
    @Override
    public HashMap<String, Object> sendDelayTestMap(MsgVO msgVO) {
        HashMap<String, Object> result = new HashMap<>(8);
        Boolean checkWord = msgVO.getCheckWord();
        if (ObjectUtil.isNotEmpty(checkWord) && checkWord){
            HashMap<String, Object> info = (HashMap<String, Object>) msgVO.getInfo();
            String content = (String) info.get("content");
            HashMap<String, Object> resultWord = indecentService.getSensitiveWordsByDFA(content, true, 1);
            if (MapUtil.isNotEmpty(resultWord)){
              return resultWord;
            }
        }
        rabbitTemplate.convertAndSend(RabbitMqConfig.DELAY_EXCHANGE, RabbitMqConfig.DELAY_ROUTING_KEY, msgVO, message -> {
            Object t = msgVO.getInfo();
            String pushTime = MapUtil.getStr((Map<?, ?>) t, "pushTime");
            // 计算时间差
            LocalDateTime pushLocalDateTime = DateUtil.parseLocalDateTime(pushTime);
            LocalDateTime nowLocalDate = DateUtil.toLocalDateTime(new Date());

            Duration duration = Duration.between(nowLocalDate,pushLocalDateTime);
            long time = duration.toMillis();
            log.info("需要延迟发布的时间：[{}]",time);
            message.getMessageProperties().setHeader("x-delay", time);
            return message;
        });
        log.info("map格式的数据发送成功 发送时间为", DateUtil.now());
        result.put("code",200);
        result.put("msg","发送成功！");
        return result;
    }
}

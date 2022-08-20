package com.mp.demo.serivce;

import com.mp.demo.entity.MsgDTO;
import com.mp.demo.entity.MsgVO;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @auther:Wangxl
 */
public interface ProducerService {

    /**
     * 发送json格式数据
     *
     * @param msgVO
     */
    HashMap<String, Object>  sendTestJson(MsgVO msgVO);


    /**
     * 延时发送map格式数据
     *
     * @param msgVO
     */
    HashMap<String, Object> sendDelayTestMap(MsgVO msgVO);
}

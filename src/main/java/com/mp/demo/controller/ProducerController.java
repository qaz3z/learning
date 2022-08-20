package com.mp.demo.controller;

import com.mp.demo.entity.MsgDTO;
import com.mp.demo.entity.MsgVO;
import com.mp.demo.serivce.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author qaz
 */
@RestController
@RequestMapping("/producer")
public class ProducerController {

    @Autowired
    private ProducerService producerService;

    @PostMapping("/sendObject")
    public HashMap<String, Object>  sendObject(@RequestBody MsgVO msgVO) {
        return producerService.sendTestJson(msgVO);
    }

    @PostMapping("/sendMap")
    public HashMap<String, Object> sendMap(@RequestBody MsgVO msgVO) {
        return producerService.sendDelayTestMap( msgVO);
    }
}

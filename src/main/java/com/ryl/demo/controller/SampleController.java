/*
package com.ryl.demo.controller;


import com.ryl.demo.rabbitmq.MQSender;
import com.ryl.demo.redis.RedisService;
import com.ryl.demo.result.Result;
import com.ryl.demo.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SampleController {

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
        mqSender.send("hello rabbitmq");
        return Result.success("Hello,world");

    }


    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic(){
        mqSender.sendTopic("hello rabbitmq");
        return Result.success("Hello,world");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout(){
        mqSender.sendFanout("hello rabbitmq");
        return Result.success("Hello,world");

    }



    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> header(){
        mqSender.sendHeader("hello rabbitmq");
        return Result.success("Hello,world");

    }






}
*/

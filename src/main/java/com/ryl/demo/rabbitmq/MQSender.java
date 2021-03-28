package com.ryl.demo.rabbitmq;


import com.ryl.demo.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {


    @Autowired
    AmqpTemplate amqpTemplate;



    public void sendMiaoshaMessage(MiaoshaMessage mm){
        String msg= RedisService.beanToString(mm);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);

    }

/*    private static Logger log= LoggerFactory.getLogger(MQReceiver.class);


    public void send(Object message){
        String msg= RedisService.beanToString(message);
        log.info("send message: "+msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE,message);

    }

    public void sendTopic(Object message){
        String msg= RedisService.beanToString(message);
        log.info("send message: "+msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key1",message+"1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key2",message+"2");
    }

    public void sendFanout(Object message){
        String msg= RedisService.beanToString(message);
        log.info("send message: "+msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",message);

    }

    public void sendHeader(Object message){
        String msg= RedisService.beanToString(message);
        log.info("send message: "+msg);
        MessageProperties properties=new MessageProperties();
        properties.setHeader("header1","value1");
        properties.setHeader("header2","value2");
        Message obj=new Message(msg.getBytes(),properties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"",obj);

    }*/

}

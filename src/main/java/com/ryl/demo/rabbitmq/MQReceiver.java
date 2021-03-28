package com.ryl.demo.rabbitmq;


import com.ryl.demo.domain.MiaoshaOrder;
import com.ryl.demo.domain.MiaoshaUser;
import com.ryl.demo.redis.RedisService;
import com.ryl.demo.result.CodeMsg;
import com.ryl.demo.result.Result;
import com.ryl.demo.service.GoodsService;
import com.ryl.demo.service.MiaoshaService;
import com.ryl.demo.service.MiaoshaUserService;
import com.ryl.demo.service.OrderService;
import com.ryl.demo.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {


    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;


    @Autowired
    GoodsService goodsService;


    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;


    private static Logger log=LoggerFactory.getLogger(MQReceiver.class);


     @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
         MiaoshaMessage mm = RedisService.stringToBean(message, MiaoshaMessage.class);
         MiaoshaUser user = mm.getUser();
         long goodId = mm.getGoodId();

         GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodId);
         Integer stock = goods.getStockCount();
         if(stock<=0){
             return;
         }
         MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodId);
         if(order!=null){
             return;
         }

         miaoshaService.miaosha(user,goods);
     }


/*    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message){
        log.info("receive message:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message){
        log.info("topic queue1 message:"+message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message){
        log.info("topic queue2 message:"+message);
    }

    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
    public void receiveHeaderQueue(byte[] message){
        log.info("header queue2 message:"+new String(message));
    }*/


}

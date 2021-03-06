package com.ryl.demo.service;


import com.alibaba.druid.util.StringUtils;
import com.ryl.demo.dao.MiaoshaUserDao;
import com.ryl.demo.domain.Goods;
import com.ryl.demo.domain.MiaoshaOrder;
import com.ryl.demo.domain.MiaoshaUser;
import com.ryl.demo.domain.OrderInfo;
import com.ryl.demo.exception.GlobalException;
import com.ryl.demo.redis.MiaoshaKey;
import com.ryl.demo.redis.MiaoshaUserKey;
import com.ryl.demo.redis.RedisService;
import com.ryl.demo.result.CodeMsg;
import com.ryl.demo.util.MD5Util;
import com.ryl.demo.util.UUIDUtil;
import com.ryl.demo.vo.GoodsVo;
import com.ryl.demo.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class MiaoshaService {


    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    public  boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {

        Integer codeOld=redisService.get(MiaoshaKey.getMiaoshaVerifyCode,""+user.getId()+","+goodsId,Integer.class);

        if(codeOld==null||codeOld-verifyCode!=0) return false;


        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode,user.getId()+","+goodsId);

        return true;
    }

    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        boolean success = goodsService.reduceStock(goods);
        if(success){
            OrderInfo orderInfo=orderService.createOrder(user,goods);
            return orderInfo;
        }else{
            setGoodsOver(goods.getId());
            return null;
        }

    }

    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if(order!=null){
            return order.getOrderId();
        }
        else {
            boolean isOver=getGoodsOver(goodsId);
            if(isOver){
                return -1;
            }
            else{
                return 0;
            }
        }
    }

    private void setGoodsOver(Long id) {
        redisService.set(MiaoshaKey.isGoodsOver,""+id,true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(MiaoshaKey.isGoodsOver,""+goodsId);
    }

    public boolean checkPath(MiaoshaUser user, long goodsId, String path) {

        if(user==null||path==null) return false;

        String pathOld = redisService.get(MiaoshaKey.getMiaoshaPath, "" + user.getId() + "_" + goodsId, String.class);

        return path.equals(pathOld);

    }

    public String createMiaoshaPath(MiaoshaUser user,long goodsId) {
        String str= MD5Util.md5(UUIDUtil.uuid()+"123456");
        redisService.set(MiaoshaKey.getMiaoshaPath,""+user.getId()+"_"+goodsId,str);
        return str;
    }

    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
        if(user==null||goodsId<0) return null;


        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //??????????????????redis???
        int rnd = calc(verifyCode);
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //????????????
        return image;

    }

    private int calc(String verifyCode) {

        try {
            ScriptEngineManager manager=new ScriptEngineManager();

            ScriptEngine javaScript = manager.getEngineByName("JavaScript");
            return (Integer)javaScript.eval(verifyCode);


        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }



    }

    private static char[] ops=new char[]{'+','-','*'};

    private String generateVerifyCode(Random random) {


        int num1 = random.nextInt(10);
        int num2=random.nextInt(10);
        int num3=random.nextInt(10);

        char op1=ops[random.nextInt(3 )];
        char op2=ops[random.nextInt(3 )];

        String exp=""+num1+op1+num2+op2+num3;
        return exp;

    }
}

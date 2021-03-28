package com.ryl.demo.controller;

import com.ryl.demo.access.AccessLimit;
import com.ryl.demo.domain.MiaoshaOrder;
import com.ryl.demo.domain.MiaoshaUser;
import com.ryl.demo.domain.OrderInfo;
import com.ryl.demo.rabbitmq.MQSender;
import com.ryl.demo.rabbitmq.MiaoshaMessage;
import com.ryl.demo.redis.*;
import com.ryl.demo.result.CodeMsg;
import com.ryl.demo.result.Result;
import com.ryl.demo.service.GoodsService;
import com.ryl.demo.service.MiaoshaService;
import com.ryl.demo.service.MiaoshaUserService;
import com.ryl.demo.service.OrderService;
import com.ryl.demo.util.MD5Util;
import com.ryl.demo.util.UUIDUtil;
import com.ryl.demo.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {


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

    @Autowired
    MQSender sender;

    private ConcurrentHashMap<Long,Boolean> localOverMap=new ConcurrentHashMap<>();

    @RequestMapping(value = "/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(/*@PathVariable("path") String path,*/
                                  long goodsId, Model model, MiaoshaUser user){


        if(user==null){
            return Result.error(CodeMsg.SERVER_ERROR);
        }

//        //验证path
//        boolean check=miaoshaService.checkPath(user,goodsId,path);
//        if(!check){
//            return Result.error(CodeMsg.REQUEST_ILLEGAL);
//        }


        boolean over = localOverMap.get(goodsId);
        if(over){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }




        //判断库存
        long stock=redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if(stock<0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }


        MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }


        //入队
        MiaoshaMessage mm=new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0);//排队中



/*        model.addAttribute("user",user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stock = goods.getStockCount();
        if(stock<=0){
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        OrderInfo orderInfo=miaoshaService.miaosha(user,goods);

        return Result.success(orderInfo);*/
    }



    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(long goodsId, Model model, MiaoshaUser user){
        model.addAttribute("user", user);
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        long result =miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);

    }


    @AccessLimit(seconds=5,maxCount=10,needLogin=true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getPath(HttpServletRequest request,int verifyCode, MiaoshaUser user, long goodsId){

        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }


        boolean check=miaoshaService.checkVerifyCode(user,goodsId,verifyCode);

        if(!check) return Result.error(CodeMsg.REQUEST_ILLEGAL);

        String path=miaoshaService.createMiaoshaPath(user,goodsId);
        return Result.success(path);

    }



    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> verifyCode(HttpServletResponse response, MiaoshaUser user, long goodsId){

        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image =miaoshaService.createVerifyCode(user,goodsId);

        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image,"JPEG",outputStream);
            outputStream.flush();
            outputStream.close();
            return null;
        }catch (Exception e){
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }





    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodlist = goodsService.listGoodsVo();
        if(goodlist==null){
            return;
        }
        for(GoodsVo goodsVo:goodlist){
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goodsVo.getId(),goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(),false);
        }

    }




}



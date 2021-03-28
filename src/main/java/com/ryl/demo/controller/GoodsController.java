package com.ryl.demo.controller;

import com.ryl.demo.domain.Goods;
import com.ryl.demo.domain.MiaoshaUser;
import com.ryl.demo.domain.User;
import com.ryl.demo.redis.GoodsKey;
import com.ryl.demo.redis.RedisService;
import com.ryl.demo.result.Result;
import com.ryl.demo.service.GoodsService;
import com.ryl.demo.service.MiaoshaUserService;
import com.ryl.demo.vo.GoodsDetailVo;
import com.ryl.demo.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Controller
@RequestMapping("/goods")
public class GoodsController {


    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;


    @Autowired
    GoodsService goodsService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @RequestMapping(value="/to_list",produces = "text/html")
    @ResponseBody
    public String to_list(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user) {


        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }


        model.addAttribute("user", user);
        List<GoodsVo> goodsVoList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsVoList);


        WebContext webContext=new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html=thymeleafViewResolver.getTemplateEngine().process("goods_list",webContext);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList, "", html);
        }
        return html;
    }

/*
    @RequestMapping(value = "/to_detail2/{goodsId}",produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user, @PathVariable("goodsId")long goodId){



        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodId, String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }





        model.addAttribute("user",user);
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now=System.currentTimeMillis();
        int miaoshaStatus=0;
        int remainSeconds=0;
        if(now<startAt){
            miaoshaStatus=0;
            remainSeconds=(int)(startAt-now)/1000;
        }else if (now>endAt){
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{
            miaoshaStatus=1;
            remainSeconds=0;
        }
        model.addAttribute("goods",goods);
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);



        WebContext webContext=new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html=thymeleafViewResolver.getTemplateEngine().process("goods_detail",webContext);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail, ""+goodId, html);
        }

        return html;


    }

*/

    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user, @PathVariable("goodsId")long goodId){

        model.addAttribute("user",user);
        GoodsVo goods=goodsService.getGoodsVoByGoodsId(goodId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now=System.currentTimeMillis();
        int miaoshaStatus=0;
        int remainSeconds=0;
        if(now<startAt){
            miaoshaStatus=0;
            remainSeconds=(int)(startAt-now)/1000;
        }else if (now>endAt){
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{
            miaoshaStatus=1;
            remainSeconds=0;
        }
//        model.addAttribute("goods",goods);
//        model.addAttribute("miaoshaStatus",miaoshaStatus);
//        model.addAttribute("remainSeconds",remainSeconds);

        GoodsDetailVo vo=new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);


    }





}

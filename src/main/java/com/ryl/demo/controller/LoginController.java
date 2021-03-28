package com.ryl.demo.controller;


import com.ryl.demo.domain.MiaoshaUser;
import com.ryl.demo.redis.MiaoshaUserKey;
import com.ryl.demo.redis.RedisService;
import com.ryl.demo.result.CodeMsg;
import com.ryl.demo.result.Result;
import com.ryl.demo.service.MiaoshaUserService;
import com.ryl.demo.util.ValidatorUtil;
import com.ryl.demo.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {


    private static Logger log= LoggerFactory.getLogger(LoginController.class);

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/to_login")
    public String toLogin(MiaoshaUser miaoshaUser){

        if(miaoshaUser!=null){
            return "goods_list";
        }
        return "login";
    }


    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, HttpServletRequest request, @Valid LoginVo loginVo){
//        log.info(loginVo.toString());
//        String password = loginVo.getPassword();
//        String mobile = loginVo.getMobile();
//        if(StringUtils.isEmpty(password)){
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
//        if(StringUtils.isEmpty(mobile)){
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        if(!ValidatorUtil.isMobile(mobile)){
//            return Result.error(CodeMsg.MOBILE_ERROR);
//        }

        String token = miaoshaUserService.login(response, loginVo);


        return Result.success(token);
    }
}

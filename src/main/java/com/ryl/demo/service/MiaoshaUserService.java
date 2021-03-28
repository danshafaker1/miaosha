package com.ryl.demo.service;


import com.alibaba.druid.util.StringUtils;
import com.ryl.demo.dao.MiaoshaUserDao;
import com.ryl.demo.exception.GlobalException;
import com.ryl.demo.redis.MiaoshaUserKey;
import com.ryl.demo.redis.RedisService;
import com.ryl.demo.result.CodeMsg;
import com.ryl.demo.domain.MiaoshaUser;
import com.ryl.demo.util.MD5Util;
import com.ryl.demo.util.UUIDUtil;
import com.ryl.demo.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public static final String COOKI_NAME_TOKEN="token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    public MiaoshaUser getById(long id){
        MiaoshaUser miaoshaUser = redisService.get(MiaoshaUserKey.getById, "id" + id, MiaoshaUser.class);
        if(miaoshaUser!=null){
            return miaoshaUser;
        }

        miaoshaUser=miaoshaUserDao.getById(id);

        if(miaoshaUser!=null){
            redisService.set(MiaoshaUserKey.getById,""+id,miaoshaUser);
        }
        return miaoshaUser;
    }

    public boolean updatePassword(String token,long id,String formPass){

        MiaoshaUser user=getById(id);
        if(user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        MiaoshaUser toBeUpdate=new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass,user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);

        redisService.delete(MiaoshaUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token,token,user);
        return true;
    }

    public String login(HttpServletResponse response, LoginVo loginVo){


        if(loginVo==null) throw new GlobalException(CodeMsg.SERVER_ERROR);
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if(user==null) throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        String dbPass = user.getPassword();
        String saltDb=user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(password, saltDb);
        if(!calcPass.equals(dbPass)) throw new GlobalException(CodeMsg.PASSWORD_ERROR);


        String token= UUIDUtil.uuid();
        addCookie(response,token,user);
        return token;
    }

    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }

        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        //延长有效期
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }


    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN+user.getId(), token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }



}

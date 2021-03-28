package com.ryl.demo.access;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.ryl.demo.domain.MiaoshaUser;
import com.ryl.demo.redis.AccessKey;
import com.ryl.demo.redis.RedisService;
import com.ryl.demo.result.CodeMsg;
import com.ryl.demo.result.Result;
import com.ryl.demo.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class AccessInterceptor implements HandlerInterceptor {

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (handler instanceof HandlerMethod) {

            MiaoshaUser user = getUser(request, response);
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }

            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();

            if (needLogin) {
                if (user == null) {
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            } else {
                // do nothing
            }


            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if (count == null) {
                redisService.set(ak, key, 1);
            } else if (count < maxCount) {
                redisService.incr(ak, key);
            } else {
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }

            return true;
    }




    private void render(HttpServletResponse response,CodeMsg codeMsg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        ServletOutputStream out = response.getOutputStream();
        String s = JSON.toJSONString(Result.error(codeMsg));
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();


    }



    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){

        String paramToken = request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String CookieToken=getCookieValue(request,MiaoshaUserService.COOKI_NAME_TOKEN);

        if(StringUtils.isEmpty(CookieToken)&&StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? CookieToken : paramToken;
        return miaoshaUserService.getByToken(response,token);



    }


    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

package com.ryl.demo.config;

import com.alibaba.druid.util.StringUtils;
import com.ryl.demo.access.UserContext;
import com.ryl.demo.domain.MiaoshaUser;
import com.ryl.demo.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Type;


@Service
public class UserArgumentResolve implements HandlerMethodArgumentResolver {


    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Type genericParameterType = methodParameter.getGenericParameterType();
        return genericParameterType== MiaoshaUser.class;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

        return UserContext.getUser();


    }

    private String getCookieValue(HttpServletRequest nativeRequest, String cookiNameToken) {

        Cookie[] cookies = nativeRequest.getCookies();
        if(cookies==null||cookies.length<=0){
            return null;
        }

        for(Cookie cookie:cookies){
            if(cookie.getName().equals(cookiNameToken)) return cookie.getValue();
        }
        return null;


    }
}

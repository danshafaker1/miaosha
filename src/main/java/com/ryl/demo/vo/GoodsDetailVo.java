package com.ryl.demo.vo;


import com.ryl.demo.domain.MiaoshaUser;
import lombok.Data;

@Data
public class GoodsDetailVo {

    private int miaoshaStatus=0;
    private int remainSeconds=0;
    private GoodsVo goods;
    private MiaoshaUser user;

}

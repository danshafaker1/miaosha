package com.ryl.demo.service;

import com.ryl.demo.dao.GoodsDao;
import com.ryl.demo.domain.Goods;
import com.ryl.demo.domain.MiaoshaGoods;
import com.ryl.demo.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class GoodsService {


    @Autowired
    GoodsDao goodsDao;


    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }


    public GoodsVo getGoodsVoByGoodsId(long goodId) {
        return goodsDao.getGoodsVoByGoodsId(goodId);

    }

    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g=new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int ret=goodsDao.reduceStock(g);
        return ret>0;
    }
}

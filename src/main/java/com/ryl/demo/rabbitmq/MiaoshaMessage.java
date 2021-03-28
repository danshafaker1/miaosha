package com.ryl.demo.rabbitmq;

import com.ryl.demo.domain.MiaoshaUser;
import lombok.Data;

@Data
public class MiaoshaMessage {
    private MiaoshaUser user;
    private long goodId;

}

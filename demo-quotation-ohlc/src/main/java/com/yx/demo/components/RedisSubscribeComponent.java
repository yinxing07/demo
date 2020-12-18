package com.yx.demo.components;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author yinxing
 * @date 2020/12/2 17:03
 * @desc redis 订阅监听
 */

@Component
public class RedisSubscribeComponent implements MessageListener {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        byte[] body = message.getBody();
        String data = (String) redisTemplate.getValueSerializer().deserialize(body);
        String[] dataArr = data.split(",");
        String contract = dataArr[0];
        double last = Double.parseDouble(dataArr[1]);
        System.out.println("redis订阅监听到消息：" + data);
    }
}

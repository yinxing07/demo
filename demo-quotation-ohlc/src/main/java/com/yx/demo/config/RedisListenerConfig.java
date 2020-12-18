package com.yx.demo.config;

import com.yx.demo.components.RedisSubscribeComponent;
import com.yx.demo.constants.RedisKeyConstant;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * @author yinxing
 * @date 2020/12/18 10:44
 * @desc
 */

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisListenerConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory factory, MessageListenerAdapter adapter){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(adapter,  new PatternTopic(RedisKeyConstant.RedisChannels.REAL_TIME_QUOT_CHANNEL));
        return container;
    }

    @Bean
    MessageListenerAdapter adapter(RedisSubscribeComponent redisSubscribe){
        return new MessageListenerAdapter(redisSubscribe, "redisSubscribe");
    }

}

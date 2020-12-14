package com.yx.demo.service.impl;

import com.yx.demo.beans.ChannelManagerBean;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yinxing
 * @date 2020/12/11 16:23
 * @desc
 */

@Component
public class BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseService.class);

    private static AtomicInteger counter = new AtomicInteger(0);

    @Scheduled(cron = "*/1 * * * * ?")
    public static void send() {
        String msg = "message " + counter.get();
        LOGGER.info("msg={}",msg);
        ConcurrentSkipListSet<String> channelIds = ChannelManagerBean.getChannelListIds();
        for (String channelId : channelIds) {
            ChannelHandlerContext ctx = ChannelManagerBean.getActiveChannel(channelId);
            if (ctx.channel().isOpen() && ctx.channel().isActive()) {
                ctx.writeAndFlush(msg);
            }
        }
        counter.getAndIncrement();
    }
}

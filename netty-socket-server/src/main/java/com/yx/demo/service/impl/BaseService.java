package com.yx.demo.service.impl;

import com.yx.demo.beans.ChannelManagerBean;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author yinxing
 * @date 2020/12/11 16:23
 * @desc
 */

public class BaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseService.class);

    public static void pushRealTimeQuotation(String msg) {
        ConcurrentSkipListSet<String> channelIds = ChannelManagerBean.getChannelListIds();
        for (String channelId : channelIds) {
            ChannelHandlerContext ctx = ChannelManagerBean.getActiveChannel(channelId);
            if (ctx.channel().isOpen() && ctx.channel().isActive()) {
                ctx.writeAndFlush(msg);
            } else {
                LOGGER.info("delete inActive channel:{}", channelId);
                ChannelManagerBean.removeChannel(channelId);
            }
        }
    }
}

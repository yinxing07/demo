package com.yx.demo.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yx.demo.beans.ChannelManagerBean;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author yinxing
 * @date 2020/12/7 15:33
 * @desc
 */
@Component
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("get a message from client:{}", msg);
        JSONObject object = JSON.parseObject(msg.toString());
        String msgBack = "verify failed!";
        String deviceId = object.getString("deviceId");
        if(object.getString("verify").equals("yx")){
            ChannelManagerBean.addChannel(deviceId, ctx);
            LOGGER.info("current connections:{}, deviceId:{}",ChannelManagerBean.getChannelListIds().size(), deviceId);
            msgBack = "message from server: bind success";
        }
        ctx.writeAndFlush(msgBack);
        LOGGER.info(String.valueOf(ChannelManagerBean.getActiveChannelList().size()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().toString();
        ChannelManagerBean.removeChannel(channelId);
        LOGGER.info("channel closed, remove channelID:{}", channelId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info("an exception occurs:cause={}", cause.getMessage());
    }

}

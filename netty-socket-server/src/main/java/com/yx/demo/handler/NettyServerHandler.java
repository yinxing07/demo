package com.yx.demo.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yx.demo.beans.ChannelManagerBean;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yinxing
 * @date 2020/12/7 15:33
 * @desc
 */

public class NettyServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("(messageReceived)get a message from client:{}", msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("(channelRead)get a message from client:{}", msg);

        JSONObject object = JSON.parseObject(msg.toString());
        String channelId = object.getString("channelId");
        ChannelManagerBean.addChannel(channelId,ctx);

        String msgBack = "message from server: bind success";
        ctx.writeAndFlush(msgBack);
        LOGGER.info(String.valueOf(ChannelManagerBean.getActiveChannelList().size()));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("handlerAdded:{}", ctx.name());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("handlerRemoved:{}", ctx.name());

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.info("exceptionCaught:{}", cause.getMessage());
    }
}

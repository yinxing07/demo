package com.yx.demo.handler;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

/**
 * @author yinxing
 * @date 2020/12/11 15:07
 * @desc
 */

public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf buf = msg.readBytes(msg.readableBytes());
        String msgStr = buf.toString(StandardCharsets.UTF_8);
        LOGGER.info("get a message from server:{}", msgStr);
    }

    /**
     * 向服务端发送数据
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("channel is active,localAddress is {}", ctx.channel().localAddress());
        JSONObject obj = new JSONObject();
        obj.put("verify", "yx");
        obj.put("deviceId", UUID.randomUUID());
        LOGGER.info("send a message to server:{}", obj);
        ctx.writeAndFlush(Unpooled.copiedBuffer(obj.toJSONString(), CharsetUtil.UTF_8)); // 必须有flush
    }

    /**
     * channelInactive
     * <p>
     * channel 通道 Inactive 不活跃的
     * <p>
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("connection was disconnected by client, channel is closed!");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        LOGGER.error("exception caught, cause:{}", cause.getMessage());
    }

}

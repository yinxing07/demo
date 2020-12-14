package com.yx.demo.startup;

import com.yx.demo.handler.NettyServerHandler;
import com.yx.demo.utils.DateUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author yinxing
 * @date 2020/12/7 15:15
 * @desc
 */

@Component
public class NettySocketServerStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettySocketServerStarter.class);

    private static final Integer PORT = 8888;

    private static final String HOST = "127.0.0.1";

    private Channel channel;

    private ServerBootstrap serverBootstrap = new ServerBootstrap();

    @PostConstruct
    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        serverBootstrap.group(bossGroup, workerGroup)
                .localAddress(new InetSocketAddress(PORT))
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8))
                                .addLast("encoder", new StringEncoder(CharsetUtil.UTF_8))
                                .addLast(new NettyServerHandler());
                    }
                });
        connect();
    }

    public void connect() throws Exception {
        if (channel != null && channel.isActive()) {
            return;
        }
        ChannelFuture future = serverBootstrap.bind(PORT).sync();
        if (future.isSuccess()) {
            LOGGER.info("netty socket server start success, now:{},port:{}", DateUtil.getCurrentDate(""),PORT);
        }
        channel = future.channel();
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                LOGGER.info("netty socket server start success:{},{}", HOST, PORT);
            } else {
                LOGGER.info("netty socket server start failed:{},{}", HOST, PORT);
                f.channel().eventLoop().schedule(() -> {
                    try {
                        connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LOGGER.info("netty connect exception");
                    }
                }, 10, TimeUnit.SECONDS);
            }
        });
    }
}

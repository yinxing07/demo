package com.yx.demo.startup;

import com.yx.demo.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NettySocketClientStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettySocketClientStarter.class);
    private Bootstrap b = new Bootstrap();

    private static final String HOST = "127.0.0.1";

    private final int PORT = 8888;

    @PostConstruct
    public void init() {
        try {
            new NettySocketClientStarter().start(); // 连接服务端并启动
            LOGGER.info("启动客户端发起连接");
        } catch (Exception e) {
            LOGGER.info("连接出错:{}", e.getMessage());
        }
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        b.group(group) // 注册线程池
                .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
//               b.channel(NioSocketChannel.class); // (3)
                .option(ChannelOption.SO_KEEPALIVE, true) // (4)
                .handler(new ChannelInitializer<SocketChannel>() { // 绑定连接初始化器
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        LOGGER.info("正在连接中...");
                        ch.pipeline().addLast(new ClientHandler())
                                .addLast("decoder", new StringDecoder(CharsetUtil.UTF_8))
                                .addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    }
                });
        connect();
    }

    private void connect() throws InterruptedException {
        ChannelFuture f = b.connect(HOST, PORT).sync();
        if (!f.isSuccess()) {
            connect();
            LOGGER.info("尝试重连！！");
            Thread.sleep(3000);
        }
    }
}

package com.luodaijun.imserver.core;

import com.luodaijun.imserver.core.handler.IMHandler;
import com.luodaijun.imserver.utils.IMConfig;
import com.luodaijun.imserver.utils.ThreadPoolFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by luodaijun on 2017/6/24.
 */
public class NettyIMServer {
    private final static Logger logger = LoggerFactory.getLogger(NettyIMServer.class);

    private EventLoopGroup bossEventLoopGroup;
    private EventLoopGroup ioEventLoopGroup;


    public void start() {

        if (IMConfig.IM_SERVER.USE_NATIVE_TRANSPORTS) {
            //Native epoll线程组，用于接受客户端连接
            bossEventLoopGroup = new EpollEventLoopGroup(2, ThreadPoolFactory.newThreadFactory("Netty-Boss"));

            //Native epoll线程组，用于接受客户端网络读写
            ioEventLoopGroup = new EpollEventLoopGroup(IMConfig.IM_SERVER.IO_THREAD_COUNT, ThreadPoolFactory.newThreadFactory("Netty-I/O"));
        } else {
            //NIO线程组，用于接受客户端连接
            bossEventLoopGroup = new NioEventLoopGroup(2, ThreadPoolFactory.newThreadFactory("Netty-Boss"));

            //NIO线程组，用于接受客户端网络读写
            ioEventLoopGroup = new NioEventLoopGroup(IMConfig.IM_SERVER.IO_THREAD_COUNT, ThreadPoolFactory.newThreadFactory("Netty-I/O"));
        }

        System.err.println("==========USE_NATIVE_TRANSPORTS=" + IMConfig.IM_SERVER.USE_NATIVE_TRANSPORTS + "===================");

        init();

        //initSSL();
    }


    private ServerBootstrap buildBootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossEventLoopGroup, ioEventLoopGroup);

        if (IMConfig.IM_SERVER.USE_NATIVE_TRANSPORTS) {
            bootstrap.channel(EpollServerSocketChannel.class);
        } else {
            bootstrap.channel(NioServerSocketChannel.class);
        }

        bootstrap.handler(new LoggingHandler(LogLevel.INFO));
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //TIME_WAIT
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 1024 * 64);
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 1024 * 64);
        bootstrap.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);

        return bootstrap;
    }

    private void init() {
        try {
            ServerBootstrap bootstrap = buildBootstrap();

            //设置I/O事件处理类
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) throws Exception {
                    addCommonHandler(channel.pipeline());
                }
            });

            //绑定端口
            bootstrap.bind(IMConfig.IM_SERVER.BIND_IP, IMConfig.IM_SERVER.PORT).sync();

            System.err.println("http Server is listening on port " + IMConfig.IM_SERVER.BIND_IP + ":" + IMConfig.IM_SERVER.PORT);
        } catch (Exception e) {
            logger.error("Failed to start http server on port " + IMConfig.IM_SERVER.BIND_IP + ":" + IMConfig.IM_SERVER.PORT, e);
            System.exit(0);
        }
    }


    private void addCommonHandler(ChannelPipeline pipeline) {
        //设置超时时间
        pipeline.addLast(new IdleStateHandler(0, 0, IMConfig.IM_SERVER.KEEPALIVE_TIMEOUT_SECONDS));

        //换行符号分割
        pipeline.addLast(new DelimiterBasedFrameDecoder(1024 * 1024, new ByteBuf[]{Unpooled.copiedBuffer("\0".getBytes()), Unpooled.copiedBuffer("\n".getBytes()), Unpooled.copiedBuffer("\r\n".getBytes())}));

        pipeline.addLast(new StringDecoder());

        pipeline.addLast(new IMHandler());
    }

    public void shutdown() {

        if (bossEventLoopGroup != null) {
            bossEventLoopGroup.shutdownGracefully();
        }

        if (ioEventLoopGroup != null) {
            ioEventLoopGroup.shutdownGracefully();
        }

        System.err.println("*********************Netty Http Server[" + IMConfig.IM_SERVER.PORT + "] closed!***********************");
    }
}
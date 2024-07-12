package com.lal.im.tcp.server;

import com.lal.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Component;

@Component
public class TimServer {

    public TimServer() {
        init();
    }

    public void init() {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG,10240) //server connection backlog size
            .option(ChannelOption.SO_REUSEADDR,true) //parameter indicating that local address and port reuse
            .childOption(ChannelOption.TCP_NODELAY,true) //disable the Nagle algorithm
            .childOption(ChannelOption.SO_KEEPALIVE,true) // heartbeat
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new StringEncoder());
                    pipeline.addLast(new StringDecoder());
//                        pipeline.addLast(new IdleStateHandler(0,0,1));
                    pipeline.addLast(new NettyServerHandler());
                }
            });
        try {
            serverBootstrap.bind(8000).sync();
            System.out.println("tcp server is running on port 8000...");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

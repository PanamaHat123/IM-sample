package com.lal.im.tcp.server;

import com.lal.im.common.codec.MessageDecoder;
import com.lal.im.common.codec.MessageEncoder;
import com.lal.im.common.codec.config.BootstrapConfig;
import com.lal.im.tcp.handler.HeartBeatHandler;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Slf4j
@Component
@ConditionalOnProperty(prefix = "tim",name = "tcpEnabled",havingValue = "true")
public class TimServer {

    @Value("${tim.tcpPort}")
    Integer tcpPort;

    ServerBootstrap serverBootstrap;

    NioEventLoopGroup bossGroup;

    NioEventLoopGroup workerGroup;

    @Autowired
    BootstrapConfig config;

    @PostConstruct
    public void init() {

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
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
                    pipeline.addLast(new MessageDecoder());
                    pipeline.addLast(new MessageEncoder());
//                        pipeline.addLast(new IdleStateHandler(0,0,1));
//                     pipeline.addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                    pipeline.addLast(new NettyServerHandler(1000,"http://127.0.0.1:8080/v1"));
                }
            });
        try {
            serverBootstrap.bind(tcpPort).sync();
            log.warn("\r\ntcp server is running on port {}...\r\n",tcpPort);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

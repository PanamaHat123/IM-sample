package com.lal.im.tcp.server;

import com.lal.im.common.codec.WebSocketMessageDecoder;
import com.lal.im.common.codec.WebSocketMessageEncoder;
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
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Slf4j
@Component
@ConditionalOnProperty(prefix = "tim",name = "webSocketEnabled",havingValue = "true")
public class TimWebSocketServer {

    @Value("${tim.webSocketPort}")
    Integer webSocketPort;

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
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                        /**
                         WebSocket server protocol, used to specify the route for client connection access: /ws
                         This handler will take care of some of the heavy and complex tasks for you It will
                         handle the handshaking actions: handshaking (close, ping, pong) ping + pong = heartbeat For WebSocket,
                         all data is transmitted in frames, and different data types correspond to different frames.
                         */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                        pipeline.addLast(new WebSocketMessageDecoder());
                        pipeline.addLast(new WebSocketMessageEncoder());
                        pipeline.addLast(new IdleStateHandler(0,0,10));
                        // pipeline.addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        pipeline.addLast(new NettyServerHandler(1000,"http://127.0.0.1:8080/v1"));
                    }
                });
        try {
            serverBootstrap.bind(webSocketPort).sync();
            log.warn("\r\ntcp websocket server is running on port {}...\r\n",webSocketPort);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

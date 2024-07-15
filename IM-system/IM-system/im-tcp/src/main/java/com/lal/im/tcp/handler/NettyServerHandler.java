package com.lal.im.tcp.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.lal.im.common.codec.proto.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    {
        String brokerId = SpringUtil.getProperty("tim.brokerId");
        log.info("tim.brokerId : {}",brokerId);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        System.out.println("receive client message ：" + JSONUtil.toJsonStr(msg));
    }
}

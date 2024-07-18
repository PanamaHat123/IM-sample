package com.lal.im.tcp.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lal.im.common.codec.pack.LoginPack;
import com.lal.im.common.codec.pack.message.ChatMessageAck;
import com.lal.im.common.codec.pack.user.LoginAckPack;
import com.lal.im.common.codec.pack.user.UserStatusChangeNotifyPack;
import com.lal.im.common.codec.proto.Message;
import com.lal.im.common.codec.proto.MessageHeader;
import com.lal.im.common.codec.proto.MessagePack;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ImConnectStatusEnum;
import com.lal.im.common.enums.command.GroupEventCommand;
import com.lal.im.common.enums.command.MessageCommand;
import com.lal.im.common.enums.command.SystemCommand;
import com.lal.im.common.enums.command.UserEventCommand;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.UserClientDto;
import com.lal.im.common.model.UserSession;
import com.lal.im.common.model.message.CheckSendMessageReq;
import com.lal.im.tcp.feign.FeignMessageService;
import com.lal.im.tcp.handler.messageProcess.MessageProcessHolder;
import com.lal.im.tcp.publish.MqMessageProducer;
import com.lal.im.tcp.redis.RedisManager;
import com.lal.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private Integer brokerId;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId,String logicUrl) {
        this.brokerId = brokerId;

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        MessageProcessHolder messageProcessHolder = SpringUtil.getBean(MessageProcessHolder.class);

        MessageHeader messageHeader = msg.getMessageHeader();
        Integer command = messageHeader.getCommand();

        // strategy pattern
        messageProcessHolder.dispatch(command, ctx, msg);

    }

    //表示 channel 处于不活动状态
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

}

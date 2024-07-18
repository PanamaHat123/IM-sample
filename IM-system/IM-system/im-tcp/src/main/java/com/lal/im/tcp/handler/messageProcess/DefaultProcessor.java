package com.lal.im.tcp.handler.messageProcess;

import com.lal.im.common.codec.proto.Message;
import com.lal.im.common.codec.proto.MessageHeader;
import com.lal.im.tcp.publish.MqMessageProducer;
import io.netty.channel.ChannelHandlerContext;

public class DefaultProcessor {

    static void process(ChannelHandlerContext ctx, Message msg){
        MessageHeader messageHeader = msg.getMessageHeader();
        Integer command = messageHeader.getCommand();
        //消息发送给 逻辑层 tomcat
        MqMessageProducer.sendMessage(msg,command);
    }

}

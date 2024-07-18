package com.lal.im.tcp.handler.messageProcess;

import com.lal.im.common.codec.proto.Message;
import com.lal.im.common.enums.command.Command;
import com.lal.im.common.enums.command.MessageCommand;
import io.netty.channel.ChannelHandlerContext;

public interface IMessageProcessor {

    Command getCommand();

    void process(ChannelHandlerContext ctx, Message msg);

}

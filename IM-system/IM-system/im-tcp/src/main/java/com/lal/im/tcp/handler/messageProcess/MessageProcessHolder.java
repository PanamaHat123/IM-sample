package com.lal.im.tcp.handler.messageProcess;


import com.lal.im.common.codec.proto.Message;
import com.lal.im.common.enums.command.Command;
import com.lal.im.common.enums.command.MessageCommand;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageProcessHolder {

    private Map<Integer,IMessageProcessor> messageProcessMap = new ConcurrentHashMap<>();


    //register all processors
    @Autowired
    private void init(List<IMessageProcessor> processors){
        for (IMessageProcessor processor : processors) {
            register(processor.getCommand(),processor);
        }
    }

    public void register(Command command,IMessageProcessor messageProcessor){
        messageProcessMap.put(command.getCommand(),messageProcessor);
    }

    public void dispatch(Integer commandInt,ChannelHandlerContext ctx, Message msg){
        IMessageProcessor processor = messageProcessMap.get(commandInt);

        if(processor==null){
            DefaultProcessor.process(ctx,msg);
        }else{
            processor.process(ctx,msg);
        }
    }

}

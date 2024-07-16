package com.lal.im.tcp.reciver.process;


import com.lal.im.common.codec.proto.MessagePack;
import com.lal.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

public abstract class BaseProcess {

    public abstract void processBefore();


    public void process(MessagePack messagePack){

        processBefore();
        NioSocketChannel socketChannel = SessionSocketHolder.get(messagePack.getAppId(), messagePack.getToId(),
                messagePack.getClientType(), messagePack.getImei());

        if(socketChannel!=null){
            socketChannel.writeAndFlush(messagePack);
        }
        processAfter();
    }

    public abstract void processAfter();

}

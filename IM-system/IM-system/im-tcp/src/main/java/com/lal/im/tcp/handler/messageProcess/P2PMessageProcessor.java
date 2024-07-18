package com.lal.im.tcp.handler.messageProcess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.codec.pack.message.ChatMessageAck;
import com.lal.im.common.codec.proto.Message;
import com.lal.im.common.codec.proto.MessageHeader;
import com.lal.im.common.codec.proto.MessagePack;
import com.lal.im.common.enums.command.Command;
import com.lal.im.common.enums.command.GroupEventCommand;
import com.lal.im.common.enums.command.MessageCommand;
import com.lal.im.common.enums.command.SystemCommand;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.message.CheckSendMessageReq;
import com.lal.im.tcp.feign.FeignMessageService;
import com.lal.im.tcp.publish.MqMessageProducer;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Service
@Slf4j
public class P2PMessageProcessor implements IMessageProcessor{

    Command command = MessageCommand.MSG_P2P;

    @Value("${tim.logicUrl}")
    String logicUrl;

    FeignMessageService feignMessageService;

    @PostConstruct
    public void init(){
        feignMessageService = Feign.builder().encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000,3500))
                .target(FeignMessageService.class,logicUrl);
    }


    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public void process(ChannelHandlerContext ctx, Message msg) {

        MessageHeader messageHeader = msg.getMessageHeader();

        Integer command = messageHeader.getCommand();
        try {
            //单聊处理
            //todo 1.调用校验消息发送发的接口
            ResponseVO responseVO =null;
            CheckSendMessageReq req = new CheckSendMessageReq();
            req.setAppId(messageHeader.getAppId());
            req.setCommand(messageHeader.getCommand());
            JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
            req.setFromId(jsonObject.getString("fromId"));


                req.setToId(jsonObject.getString("toId"));
                responseVO =  feignMessageService.checkSendMessage(req);


            if(responseVO.isOk()){
                //如果成功 投递到mq
                //消息发送给 逻辑层 tomcat
                MqMessageProducer.sendMessage(msg,command);
            }else{

                //失败则 直接ack
                Integer ackCommand = 0;

                    ackCommand = MessageCommand.MSG_ACK.getCommand();

                ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                responseVO.setData(chatMessageAck);
                MessagePack<ResponseVO> ack = new MessagePack<>();
                ack.setData(responseVO);
                ack.setCommand(ackCommand);
                ctx.channel().writeAndFlush(ack);
            }
        }catch (Exception e){
            log.error("P2P message process error,error: {}" ,e.getMessage());
        }

    }
}

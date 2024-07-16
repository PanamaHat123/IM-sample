package com.lal.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.codec.proto.MessagePack;
import com.lal.im.common.constant.Constants;
import com.lal.im.tcp.reciver.process.BaseProcess;
import com.lal.im.tcp.reciver.process.ProcessFactory;
import com.lal.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Slf4j
public class MessageReciver {

    private static String brokerId;

    private static void startReciverMessage(){

        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MessageService2Im + brokerId);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im + brokerId,
                    true,false,false,null);

            channel.queueBind(Constants.RabbitConstants.MessageService2Im + brokerId,Constants.RabbitConstants.MessageService2Im,brokerId);

            channel.basicConsume(Constants.RabbitConstants.MessageService2Im+brokerId,false,new DefaultConsumer(channel){

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //todo 处理消息服务发来的消息
                    try {
                        String msgStr = new String(body);
                        log.info("收到逻辑层发来的消息：{}",msgStr);

                        MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);

                        BaseProcess messageProcess = ProcessFactory.getMessageProcess(messagePack.getCommand());

                        messageProcess.process(messagePack);

                        // arg0 标记符 arg1 是否批量提交
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (Exception e) {
                       e.printStackTrace();
                       channel.basicNack(envelope.getDeliveryTag(),false,false);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init(){
        startReciverMessage();
    }

    public static void init(String brokerId){
        if(StringUtils.isBlank(MessageReciver.brokerId)){
            MessageReciver.brokerId = brokerId;
        }
        startReciverMessage();
    }

}

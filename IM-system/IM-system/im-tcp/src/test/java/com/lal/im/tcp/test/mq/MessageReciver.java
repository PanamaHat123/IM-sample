package com.lal.im.tcp.test.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class MessageReciver {


    private static void startReciverMessage(String channelName,String queue,String exchange, String routingKey){

        try {
            Channel channel = MqFactory.getChannel(channelName);
            channel.queueDeclare(queue,
                    true,false,false,null);

            channel.queueBind(queue,exchange,routingKey);

            channel.basicConsume(queue,false,new DefaultConsumer(channel){

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //todo 处理消息服务发来的消息
                    try {
                        String msgStr = new String(body);
                        log.info("queue {}, exchange {}, routingKey {}",queue,exchange,routingKey);
                        log.info("receive mq message：{}",msgStr);

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

    public static void init(String channelName,String queue,String exchange, String routingKey){
        startReciverMessage(channelName,queue,exchange,routingKey);
    }



}

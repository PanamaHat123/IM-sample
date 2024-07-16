package com.lal.im.service.message.mq;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.command.GroupEventCommand;
import com.lal.im.common.model.message.GroupChatMessageContent;
import com.lal.im.common.model.message.MessageReadedContent;
import com.lal.im.service.message.service.GroupMessageService;
import com.lal.im.service.message.service.MessageSyncService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class GroupChatOperateReceiver {


//    @Autowired
//    P2PMessageService p2PMessageService;
    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    /**
     * 监听  im服务 发送过来的消息
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitConstants.Im2GroupService,durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitConstants.Im2GroupService,durable = "true")
            ),concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String ,Object> headers,
                              Channel channel ) throws Exception {

        String msg = new String(message.getBody(),"utf-8");
        log.info("CHAT MSG FROM QUEUE ::: {}",msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if(command.equals(GroupEventCommand.MSG_GROUP.getCommand())){
                //处理消息
                GroupChatMessageContent messageContent = jsonObject.toJavaObject(GroupChatMessageContent.class);
                groupMessageService.process(messageContent);

            }else if (command.equals(GroupEventCommand.MSG_GROUP_READED.getCommand())){
                MessageReadedContent messageReaded = jsonObject.toJavaObject(MessageReadedContent.class);
                messageSyncService.groupReadMark(messageReaded);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理消息出现异常：{}", e.getMessage());
            log.error("RMQ_CHAT_TRAN_ERROR", e);
            log.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }


    }

}

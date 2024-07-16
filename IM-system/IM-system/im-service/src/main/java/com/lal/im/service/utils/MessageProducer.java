package com.lal.im.service.utils;


import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.codec.proto.MessagePack;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.command.Command;
import com.lal.im.common.model.ClientInfo;
import com.lal.im.common.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 *
 * rabbitmq  发消息给 im层
 */
@Component
@Slf4j
public class MessageProducer {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    UserSessionUtils userSessionUtils;

    private String queueName = Constants.RabbitConstants.MessageService2Im;

    public boolean sendMessage(UserSession session , Object msg){

        try {
            log.info("send message 2 im ===> {}",msg);
            rabbitTemplate.convertAndSend(queueName,session.getBrokerId()+"",msg);
            return true;
        } catch (Exception e) {
            log.error("send message 2 im error:{}",e.getMessage());
            return false;
        }

    }

    //包装数据，调用sendMessage
    public boolean sendPack(String toId, Command command, Object msg, UserSession session){

        MessagePack messagePack = new MessagePack();
        messagePack.setCommand(command.getCommand());
        messagePack.setToId(toId);
        messagePack.setClientType(session.getClientType());
        messagePack.setAppId(session.getAppId());
        messagePack.setImei(session.getImei());

        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(msg));
        messagePack.setData(jsonObject);

        String body = JSONObject.toJSONString(messagePack);
        boolean b = sendMessage(session, body);

        return b;
    }

    //发送给某个用户的所有端的方法
    public List<ClientInfo> sendToUser(String toId, Command command, Object data, Integer appId){

        List<UserSession> userSessions = userSessionUtils.getUserSession(appId, toId);
        List<ClientInfo> list = new ArrayList<>();
        for (UserSession userSession : userSessions) {
            boolean b = sendPack(toId, command, data, userSession);
            if(b){
                list.add(new ClientInfo(userSession.getAppId(), userSession.getClientType(), userSession.getImei()));
            }
        }
        return list;
    }
    public void sendToUser(String toId,Integer clientType,String imei,Command command, Object data,Integer appId){

        if(clientType != null && StringUtils.isNotBlank(imei)){
            // 是app调用
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUserExceptClient(toId,command,data,clientInfo);
        }else{
            //可能是后台管理员操作，发送给所有的端  ，后台管理员操作没有imei号
            sendToUser(toId,command,data,appId);
        }
    }


    //发送给某个用户的指定客户端
    public void sendToUser(String toId, Command command, Object data, ClientInfo clientInfo){

        UserSession userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId,clientInfo.getClientType(),clientInfo.getImei());
        sendPack(toId,command,data,userSession);
    }


    //发送给 除了某一端 的其他端
    public void sendToUserExceptClient(String toId,Command command, Object data,ClientInfo clientInfo){

        List<UserSession> userSessions = userSessionUtils.getUserSession(clientInfo.getAppId(), toId);
        for (UserSession userSession : userSessions) {
            if(!isMatch(userSession,clientInfo)){
                sendPack(toId,command,data,userSession);
            }
        }
    }

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

}

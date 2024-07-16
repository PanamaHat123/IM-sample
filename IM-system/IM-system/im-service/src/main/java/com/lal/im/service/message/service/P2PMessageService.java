package com.lal.im.service.message.service;


import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.codec.pack.message.ChatMessageAck;
import com.lal.im.common.codec.pack.message.MessageReciveServerAckPack;
import com.lal.im.common.config.AppConfig;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ConversationTypeEnum;
import com.lal.im.common.enums.command.MessageCommand;
import com.lal.im.common.model.ClientInfo;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.message.MessageContent;
import com.lal.im.common.model.message.OfflineMessageContent;
import com.lal.im.service.message.model.req.SendMessageReq;
import com.lal.im.service.message.model.resp.SendMessageResp;
import com.lal.im.service.utils.CallbackService;
import com.lal.im.service.utils.ConversationIdGenerate;
import com.lal.im.service.utils.MessageProducer;
import com.lal.im.service.utils.RedisSeq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class P2PMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);

        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });
    }

    public void process(MessageContent messageContent){

        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        Integer appId = messageContent.getAppId();

        //todo 从缓存中取messageId   处理消息重发的问题
        MessageContent messageCache = messageStoreService.getMessageFromMessageIdCache(appId,
                messageContent.getMessageId(),MessageContent.class);
        if(messageCache!=null){
            //有缓存  不需要消息持有化
            threadPoolExecutor.execute(()->{
                //1. 回ack成功给自己 1046
                ack(messageCache, ResponseVO.successResponse());
                //2.发消息给同步的在线端
                syncToSender(messageCache,messageCache);
                //3.发消息给对方的在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageCache);
                if(clientInfos.isEmpty()){
                    //发送接收确认给发送方，要当上服务端发送的标识
                    receiverAck(messageCache);
                }
            });
            return;
        }
        //回调 用于拓展  陌生人聊天策略（默默，抖音）
        ResponseVO responseVO = ResponseVO.successResponse();
        if(appConfig.isSendMessageAfterCallback()){
            responseVO = callbackService.beforeCallback(messageContent.getAppId(), Constants.CallbackCommand.SendMessageBefore
                    , JSONObject.toJSONString(messageContent));
        }

        if(!responseVO.isOk()){
            ack(messageContent,responseVO);
            return;
        }


        // redis生成递增序列号 保证消息的有序性
        //appId+Seq+ (from + to )  groupId
        long seq = redisSeq.doGetSeq(messageContent.getAppId()+":"+ Constants.SeqConstants.Message+":"+
                ConversationIdGenerate.generateP2PId(messageContent.getFromId(),messageContent.getToId()));
        messageContent.setMessageSequence(seq);

        //前置校验
        //这个用户是否被禁言 是否被禁用
        //发送方和接收方是否是好友
//        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
//        if(responseVO.isOk()){
            threadPoolExecutor.execute(()->{


                messageStoreService.storeP2PMessage(messageContent);

                // todo 插入离线消息
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                BeanUtils.copyProperties(messageContent,offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
                messageStoreService.storeOfflineMessage(offlineMessageContent);


                //插入数据
                //1. 回ack成功给自己
                ack(messageContent,ResponseVO.successResponse());
                //2.发消息给同步的在线端
                syncToSender(messageContent,messageContent);
                //3.发消息给对方的在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageContent);

                //todo 将messageId存到缓存中  处理消息重发的问题
                messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(), messageContent);

                if(clientInfos.isEmpty()){
                    //发送接收确认给发送方，要当上服务端发送的标识
                    receiverAck(messageContent);
                }

                if(appConfig.isSendMessageAfterCallback()){
                    callbackService.callback(messageContent.getAppId(),Constants.CallbackCommand.SendMessageAfter,
                            JSONObject.toJSONString(messageContent));
                }

                log.info("消息处理完成：{}",messageContent.getMessageId());
            });
//        }else{
//
//            //告诉客户端 失败了
//            //ack
//            ack(messageContent,responseVO);
//        }



    }
    private List<ClientInfo> dispatchMessage(MessageContent messageContent){

        List<ClientInfo> list = messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P, messageContent, messageContent.getAppId());
        return list;
    }


    private void ack(MessageContent messageContent,ResponseVO responseVO){
        log.info("msg ack,msgId={},checkResult={}",messageContent.getMessageId(),responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId(),messageContent.getMessageSequence());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK,responseVO,messageContent);
    }

    public void receiverAck(MessageContent messageContent){
        MessageReciveServerAckPack pack = new MessageReciveServerAckPack();
        pack.setFromId(messageContent.getToId());
        pack.setToId(messageContent.getToId());
        pack.setMessageKey(messageContent.getMessageKey());
        pack.setMessageSequence(messageContent.getMessageSequence());
        pack.setServerSend(true);
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_RECIVE_ACK,pack,new ClientInfo(messageContent.getAppId(),messageContent.getClientType(),
                messageContent.getImei()));

    }


    //发消息给同步的在线端
    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                MessageCommand.MSG_P2P,messageContent,messageContent);

    }


    //前置校验
    public ResponseVO imServerPermissionCheck(String fromId,String toId,Integer appId){

        ResponseVO responseVO = checkSendMessageService.checkSenderForvidAndMute(fromId, appId);
        if(!responseVO.isOk()){
            return responseVO;
        }

        responseVO = checkSendMessageService.checkFriendShip(fromId, toId, appId);
        return responseVO;
    }


    public SendMessageResp send(SendMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        MessageContent message = new MessageContent();
        BeanUtils.copyProperties(req,message);


        //持久化
        //插入数据
        messageStoreService.storeP2PMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        //2.发消息给同步的在线端
        syncToSender(message,message);
        //3.发消息给对方的在线端
        dispatchMessage(message);
        return sendMessageResp;
    }


}

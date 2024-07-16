package com.lal.im.service.message.service;


import com.lal.im.common.codec.pack.message.ChatMessageAck;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.command.GroupEventCommand;
import com.lal.im.common.model.ClientInfo;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.message.GroupChatMessageContent;
import com.lal.im.common.model.message.OfflineMessageContent;
import com.lal.im.service.group.model.req.SendGroupMessageReq;
import com.lal.im.service.group.service.ImGroupMemberService;
import com.lal.im.service.message.model.resp.SendMessageResp;
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
public class GroupMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    RedisSeq redisSeq;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        AtomicInteger num = new AtomicInteger(0);

        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("message-group-process-thread-" + num.getAndIncrement());
                return thread;
            }
        });

    }


    public void process(GroupChatMessageContent messageContent){

        String fromId = messageContent.getFromId();
        String groupId = messageContent.getGroupId(); //群 ID
        Integer appId = messageContent.getAppId();

        GroupChatMessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(appId,
                messageContent.getMessageId(), GroupChatMessageContent.class);
        if(messageFromMessageIdCache!=null){
            //之前处理过这条消息

            threadPoolExecutor.execute(()->{
                //1. 回ack成功给自己
                ack(messageFromMessageIdCache, ResponseVO.successResponse());
                //2.发消息给同步的在线端
                syncToSender(messageFromMessageIdCache,messageFromMessageIdCache);
                //3.发消息给对方的在线端
                dispatchMessage(messageFromMessageIdCache);
            });
            return;
        }


        //前置校验  现在放到了TCP层做校验
//        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId,appId);
//        if(responseVO.isOk()){

        long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":"
                + Constants.SeqConstants.GroupMessage
                + ":" + messageContent.getGroupId());
        messageContent.setMessageSequence(seq);


        threadPoolExecutor.execute(()->{
            messageStoreService.storeGroupMessage(messageContent);


            //插入离线消息
            List<String> groupMemberIds = imGroupMemberService.getGroupMemberId(messageContent.getGroupId(), messageContent.getAppId());
            messageContent.setMemberId(groupMemberIds);
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent,offlineMessageContent);
            offlineMessageContent.setToId(messageContent.getGroupId());
            messageStoreService.storeGroupOfflineMessage(offlineMessageContent,groupMemberIds);

            //1. 回ack成功给自己
            ack(messageContent,ResponseVO.successResponse());
            //2.发消息给同步的在线端
            syncToSender(messageContent,messageContent);
            //3.发消息给对方的在线端
            dispatchMessage(messageContent);

            //消息存入redis
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(), messageContent.getMessageId(),messageContent);

        });

    }
    private void dispatchMessage(GroupChatMessageContent messageContent){

        List<String> groupMemberIds = messageContent.getMemberId();

        for (String memberId : groupMemberIds) {
            //排除群中的自己 ，之前已经发送给自己的其他端了
            if(!memberId.equals(messageContent.getFromId())){
                messageProducer.sendToUser(memberId, GroupEventCommand.MSG_GROUP,messageContent,messageContent.getAppId());
            }
        }



    }


    private void ack(GroupChatMessageContent messageContent,ResponseVO responseVO){
        log.info("msg ack,msgId={},checkResult={}",messageContent.getMessageId(),responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        responseVO.setData(chatMessageAck);
        //发消息
        messageProducer.sendToUser(messageContent.getFromId(), GroupEventCommand.GROUP_MSG_ACK,responseVO,messageContent);
    }

    //发消息给同步的在线端
    private void syncToSender(GroupChatMessageContent messageContent, ClientInfo clientInfo){
        messageProducer.sendToUserExceptClient(messageContent.getFromId(),
                GroupEventCommand.MSG_GROUP,messageContent,messageContent);

    }


    //前置校验
    public ResponseVO imServerPermissionCheck(String fromId,String toId,Integer appId){
        ResponseVO responseVO = checkSendMessageService.checkGroupMessage(fromId, toId,appId);
        return responseVO;
    }


    public SendMessageResp send(SendGroupMessageReq req) {

        SendMessageResp sendMessageResp = new SendMessageResp();
        GroupChatMessageContent message = new GroupChatMessageContent();
        BeanUtils.copyProperties(req,message);


        //消息存到数据库
        messageStoreService.storeGroupMessage(message);
        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        //2.发消息给同步的在线端
        syncToSender(message,message);
        //3.发消息给对方的在线端
        dispatchMessage(message);

        return sendMessageResp;

    }
}

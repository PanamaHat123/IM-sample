package com.lal.im.service.message.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lal.im.common.codec.pack.message.MessageReadedPack;
import com.lal.im.common.codec.pack.message.RecallMessageNotifyPack;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ConversationTypeEnum;
import com.lal.im.common.enums.DelFlagEnum;
import com.lal.im.common.enums.MessageErrorCode;
import com.lal.im.common.enums.command.Command;
import com.lal.im.common.enums.command.GroupEventCommand;
import com.lal.im.common.enums.command.MessageCommand;
import com.lal.im.common.model.ClientInfo;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.SyncReq;
import com.lal.im.common.model.SyncResp;
import com.lal.im.common.model.message.MessageReadedContent;
import com.lal.im.common.model.message.MessageReciveAckContent;
import com.lal.im.common.model.message.OfflineMessageContent;
import com.lal.im.common.model.message.RecallMessageContent;
import com.lal.im.service.conversation.service.ConversationService;
import com.lal.im.service.group.service.ImGroupMemberService;
import com.lal.im.service.message.mapper.ImMessageBodyMapper;
import com.lal.im.service.message.model.entity.ImMessageBodyEntity;
import com.lal.im.service.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ConversationService conversationService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    GroupMessageProducer groupMessageProducer;


    public void receiveMark(MessageReciveAckContent messageReciveAckContent){

        messageProducer.sendToUser(messageReciveAckContent.getToId(), MessageCommand.MSG_RECIVE_ACK,messageReciveAckContent,
                messageReciveAckContent.getAppId());

    }

    /**
     * 消息已读
     * 1. 更新会话的seq
     * 2. 通知在线的同步端发送指定的command ，
     * 3. 通知对方发送已读回执
     * @param messageContent
     */
    public void readMark(MessageReadedContent messageContent) {

        //跟新会话
        conversationService.messageMarkRead(messageContent);

        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageContent,messageReadedPack);
        //发送给同步端
        syncToSender(messageReadedPack,messageContent,MessageCommand.MSG_READED_NOTIFY);

        //发送给对方  发送回执
        messageProducer.sendToUser(messageContent.getToId(),
                MessageCommand.MSG_READED_RECEIPT,messageReadedPack,messageContent.getAppId());

    }

    private void syncToSender(MessageReadedPack messageReadedPack, MessageReadedContent messageReadedContent, Command command){

        //发送给自己的其他端
        messageProducer.sendToUserExceptClient(messageReadedContent.getFromId(),
                command ,messageReadedPack,messageReadedContent);

    }

    public void groupReadMark(MessageReadedContent messageReaded) {
        //跟新会话
        conversationService.messageMarkRead(messageReaded);

        MessageReadedPack messageReadedPack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded,messageReadedPack);
        //发送给同步端
        syncToSender(messageReadedPack,messageReaded, GroupEventCommand.MSG_GROUP_READED);


        //发送给群  发送回执
        messageProducer.sendToUser(messageReadedPack.getToId(),
                GroupEventCommand.MSG_GROUP_READED_RECEIPT,messageReaded,messageReaded.getAppId());

    }

    public ResponseVO syncOfflineMessage(SyncReq req) {

        SyncResp<OfflineMessageContent> resp = new SyncResp<>();
        String key = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperater();
        //获取最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if(!CollectionUtils.isEmpty(set)){
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);

            maxSeq = o.getScore().longValue();
        }

        List<OfflineMessageContent> respList = new ArrayList<>();
        resp.setMaxSequence(maxSeq);

        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key, req.getLastSequence(), maxSeq, 0, req.getMaxLimit());

        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {

            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);

        }
        resp.setDataList(respList);
        if(respList.size()>0){
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq<=offlineMessageContent.getMessageKey());
        }
        return ResponseVO.successResponse(resp);
    }

    /**
     * 撤回消息流程
     * 修改历史消息的状态
     * 修改离线消息的状态
     * ack 发送方
     * 发送给同步端
     * 分发给消息的接收方
     */
    public void recallMessage(RecallMessageContent content) {

        Long messageTime = content.getMessageTime();
        Long now = System.currentTimeMillis();

        RecallMessageNotifyPack pack = new RecallMessageNotifyPack();
        BeanUtils.copyProperties(content,pack);

        if(120000L < now - messageTime){
            // todo ack失败  超过一定时间的消息不能撤回
            recallAck(pack,ResponseVO.errorResponse(MessageErrorCode.MESSAGE_RECALL_TIME_OUT),content);
            return;
        }

        //     修改历史消息的状态
        QueryWrapper<ImMessageBodyEntity> query = new QueryWrapper<>();
        query.eq("app_id",content.getAppId());
        query.eq("message_key",content.getMessageKey());
        ImMessageBodyEntity body = imMessageBodyMapper.selectOne(query);

        if(body == null){
            // todo ack失败  不存在的消息不能撤回
            recallAck(pack,ResponseVO.errorResponse(MessageErrorCode.MESSAGEBODY_IS_NOT_EXIST),content);
            return;
        }
        if(body.getDelFlag() == DelFlagEnum.DELETE.getCode()){
            // todo ack失败  已经撤回的消息不能撤回
            recallAck(pack,ResponseVO.errorResponse(MessageErrorCode.MESSAGE_IS_RECALLED),content);
            return;
        }

        body.setDelFlag(DelFlagEnum.DELETE.getCode());
        imMessageBodyMapper.update(body,query);


        //修改离线消息
        if(content.getConversationType() == ConversationTypeEnum.P2P.getCode()){
            //单聊
            // 找到fromId的队列
            String fromKey = content.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + content.getFromId();
            // 找到toId的队列
            String toKey = content.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + content.getToId();

            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(content,offlineMessageContent);
            offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
            offlineMessageContent.setMessageKey(content.getMessageKey());
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                    ,content.getFromId(),content.getToId()));
            offlineMessageContent.setMessageBody(body.getMessageBody());

            long seq = redisSeq.doGetSeq(content.getAppId() + ":" + Constants.SeqConstants.Message+":"+ ConversationIdGenerate.generateP2PId(content.getFromId(),content.getToId()));
            offlineMessageContent.setMessageSequence(seq);


            long messageKey = SnowflakeIdWorker.nextId();
            redisTemplate.opsForZSet().add(fromKey,JSONObject.toJSONString(offlineMessageContent),messageKey);
            redisTemplate.opsForZSet().add(toKey,JSONObject.toJSONString(offlineMessageContent),messageKey);

            //ack
            recallAck(pack,ResponseVO.successResponse(),content);

            //分发给同步端
            messageProducer.sendToUserExceptClient(content.getFromId(), MessageCommand.MSG_RECALL_NOTIFY,pack,content);
            //分发给对方
            messageProducer.sendToUser(content.getToId(), MessageCommand.MSG_RECALL_NOTIFY,pack,content.getAppId());
        }else{
            //群聊
            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(content.getToId(), content.getAppId());
            long seq = redisSeq.doGetSeq(content.getAppId() + ":" + Constants.SeqConstants.Message + ":" + ConversationIdGenerate.generateP2PId(content.getFromId(),content.getToId()));
            //ack
            recallAck(pack,ResponseVO.successResponse(),content);
            //发送给同步端
            messageProducer.sendToUserExceptClient(content.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack
                    , content);
            for (String memberId : groupMemberId) {
                String toKey = content.getAppId() + ":" + Constants.SeqConstants.Message + ":" + memberId;
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
                BeanUtils.copyProperties(content,offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
                offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                        ,content.getFromId(),content.getToId()));
                offlineMessageContent.setMessageBody(body.getMessageBody());
                offlineMessageContent.setMessageSequence(seq);
                redisTemplate.opsForZSet().add(toKey,JSONObject.toJSONString(offlineMessageContent),seq);

                groupMessageProducer.producer(content.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack,content);
            }


        }


    }


    private void recallAck(RecallMessageNotifyPack recallPack, ResponseVO<Object> success, ClientInfo clientInfo) {
        ResponseVO<Object> wrappedResp = success;
        messageProducer.sendToUser(recallPack.getFromId(),
                MessageCommand.MSG_RECALL_ACK, wrappedResp, clientInfo);
    }

}

package com.lal.im.service.message.service;


import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.config.AppConfig;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ConversationTypeEnum;
import com.lal.im.common.enums.DelFlagEnum;
import com.lal.im.common.model.message.*;
import com.lal.im.service.conversation.service.ConversationService;
import com.lal.im.service.group.mapper.ImGroupMessageHistoryMapper;
import com.lal.im.service.group.model.entity.ImGroupMessageHistoryEntity;
import com.lal.im.service.message.mapper.ImMessageBodyMapper;
import com.lal.im.service.message.mapper.ImMessageHistoryMapper;
import com.lal.im.service.message.model.entity.ImMessageBodyEntity;
import com.lal.im.service.message.model.entity.ImMessageHistoryEntity;
import com.lal.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ConversationService conversationService;

    @Autowired
    AppConfig appConfig;


    @Transactional(rollbackFor = Exception.class)
    public void storeP2PMessage(MessageContent messageContent) {
        // 写扩散 操作

        // messageContent 转化成 messageBody
        ImMessageBody imMessageBody = extractMessageBody(messageContent);

//        // 插入messageBody
//        imMessageBodyMapper.insert(imMessageBodyEntity);
//        // 转化成MessageHistory
//        List<ImMessageHistoryEntity> list = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
//        //批量插入
//        imMessageHistoryMapper.insertBatchSomeColumn(list);
//        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());

        //优化
        DoStoreP2PMessageDto dto = new DoStoreP2PMessageDto();
        dto.setMessageContent(messageContent);
        dto.setMessageBody(imMessageBody);
        messageContent.setMessageKey(imMessageBody.getMessageKey());

        //todo 发送消息
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage, "", JSONObject.toJSONString(dto));
    }

    public ImMessageBody extractMessageBody(MessageContent messageContent) {

        ImMessageBody messageBody = new ImMessageBody();
        messageBody.setAppId(messageContent.getAppId());
        messageBody.setMessageKey(SnowflakeIdWorker.nextId());
        messageBody.setCreateTime(System.currentTimeMillis());
        messageBody.setSecurityKey("");
        messageBody.setExtra(messageContent.getExtra());
        messageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        messageBody.setMessageTime(messageContent.getMessageTime());
        messageBody.setMessageBody(messageContent.getMessageBody());
        return messageBody;
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity) {

        List<ImMessageHistoryEntity> list = new ArrayList<>();

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());


        list.add(fromHistory);
        list.add(toHistory);
        return list;

    }

    @Transactional(rollbackFor = Exception.class)
    public void storeGroupMessage(GroupChatMessageContent messageContent) {

        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        DoStoreGroupMessageDto dto = new DoStoreGroupMessageDto();
        dto.setMessageBody(imMessageBody);
        dto.setGroupChatMessageContent(messageContent);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreGroupMessage, "",
                JSONObject.toJSONString(dto));
        messageContent.setMessageKey(imMessageBody.getMessageKey());
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent,
                                                                     ImMessageBodyEntity messageBodyEntity) {
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        return result;
    }

    public void setMessageFromMessageIdCache(Integer appId, String messageId, MessageContent messageContent) {

        // key:  appid:cache:messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(messageContent), 300, TimeUnit.SECONDS);

    }

    public <T> T getMessageFromMessageIdCache(Integer appId,
                                              String messageId,
                                              Class<T> clazz) {
        // key:  appid:cache:messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }

    /**
     * 存储 单人 离线消息
     *
     * @param offlineMessage 1限制用户的数据的数量  *选用
     *                       2限制时间维度
     */
    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {


        // 找到 fromId的队列
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        // 找到 toId的队列
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage  + ":" + offlineMessage.getToId();

        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        //判断 队列中的数据是否超过设定值
        if(operations.zCard(fromKey)>appConfig.getOfflineMessageCount()){
            operations.removeRange(fromKey,0,0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getFromId(), offlineMessage.getToId()
        ));
        //插入 数据  根据 messageKey  作为分值
        operations.add(fromKey,JSONObject.toJSONString(offlineMessage),offlineMessage.getMessageKey());

        //判断 队列中的数据是否超过设定值
        if(operations.zCard(toKey)>appConfig.getOfflineMessageCount()){
            operations.removeRange(toKey,0,0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(
                ConversationTypeEnum.P2P.getCode(),offlineMessage.getToId(), offlineMessage.getFromId()
        ));
        //插入 数据  根据 messageKey  作为分值
        operations.add(toKey,JSONObject.toJSONString(offlineMessage),offlineMessage.getMessageKey());


    }


    /**
     * 存储 群聊 离线消息
     *
     * @param offlineMessage 1限制用户的数据的数量  *选用
     *                       2限制时间维度
     */
    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage,List<String> memberIds) {



        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();

        offlineMessage.setConversationType(ConversationTypeEnum.GROUP.getCode());


        for (String memberId : memberIds) {
            // 找到 toId的队列
            String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage  + ":" + memberId;

            offlineMessage.setConversationId(conversationService.convertConversationId(
                    ConversationTypeEnum.GROUP.getCode(),memberId, offlineMessage.getToId()
            ));
            //判断 队列中的数据是否超过设定值
            if(operations.zCard(toKey)>appConfig.getOfflineMessageCount()){
                operations.removeRange(toKey,0,0);
            }
            //插入 数据  根据 messageKey  作为分值
            operations.add(toKey,JSONObject.toJSONString(offlineMessage),offlineMessage.getMessageKey());
        }

    }


}

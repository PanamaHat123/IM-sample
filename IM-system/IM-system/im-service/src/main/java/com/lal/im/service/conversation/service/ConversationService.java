package com.lal.im.service.conversation.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lal.im.common.codec.pack.conversation.DeleteConversationPack;
import com.lal.im.common.codec.pack.conversation.UpdateConversationPack;
import com.lal.im.common.config.AppConfig;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ConversationErrorCode;
import com.lal.im.common.enums.ConversationTypeEnum;
import com.lal.im.common.enums.command.ConversationEventCommand;
import com.lal.im.common.model.ClientInfo;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.SyncReq;
import com.lal.im.common.model.SyncResp;
import com.lal.im.common.model.message.MessageReadedContent;
import com.lal.im.service.conversation.mapper.ImConversationSetMapper;
import com.lal.im.service.conversation.model.DeleteConversationReq;
import com.lal.im.service.conversation.model.UpdateConversationReq;
import com.lal.im.service.conversation.model.entity.ImConversationSetEntity;
import com.lal.im.service.utils.MessageProducer;
import com.lal.im.service.utils.RedisSeq;
import com.lal.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    @Autowired
    ImConversationSetMapper imConversationSetMapper;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    AppConfig appConfig;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    public String convertConversationId(Integer type, String fromId, String toId) {

        return type + "_" + fromId + "_" + toId;
    }

    public void messageMarkRead(MessageReadedContent messageReadedContent){

        String toId = messageReadedContent.getToId();
        //是群消息
        if(messageReadedContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()){
            toId = messageReadedContent.getGroupId();
        }

        String conversationId = convertConversationId(messageReadedContent.getConversationType(),
                messageReadedContent.getFromId(), toId);
        LambdaQueryWrapper<ImConversationSetEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImConversationSetEntity::getConversationId,conversationId);
        wrapper.eq(ImConversationSetEntity::getAppId,messageReadedContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(wrapper);
        if(imConversationSetEntity == null){
            //没有 则新建一个
            imConversationSetEntity = new ImConversationSetEntity();
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadedContent,imConversationSetEntity);
            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);

            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(), Constants.SeqConstants.Conversation,seq);
        }else{
            //更新
            long seq = redisSeq.doGetSeq(messageReadedContent.getAppId() + ":" + Constants.SeqConstants.Conversation);

            imConversationSetEntity.setReadedSequence(messageReadedContent.getMessageSequence());
            imConversationSetEntity.setSequence(seq);

            imConversationSetMapper.readMark(imConversationSetEntity);

            writeUserSeq.writeUserSeq(messageReadedContent.getAppId(), messageReadedContent.getFromId(), Constants.SeqConstants.Conversation,seq);
        }

    }


    /**
     * 删除会话
     * @param req
     * @return
     */
    public ResponseVO deleteConversation(DeleteConversationReq req){

        //删除会话
//        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("conversation_id",req.getConversationId());
//        queryWrapper.eq("app_id",req.getAppId());
//        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
//        if(imConversationSetEntity != null){
//            imConversationSetEntity.setIsMute(0);
//            imConversationSetEntity.setIsTop(0);
//            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
//        }


        //需要删除其他端的会话
        if(appConfig.getDeleteConversationSyncMode()==1){
            DeleteConversationPack pack = new DeleteConversationPack();
            pack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_DELETE,pack,
                    new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }

        return ResponseVO.successResponse();
    }

    /**
     * 更新会话  置顶 or 免打扰
     * @param req
     * @return
     */
    public ResponseVO updateConversation(UpdateConversationReq req){





        if(req.getIsTop() == null && req.getIsMute() == null){
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id",req.getConversationId());
        queryWrapper.eq("app_id",req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if(imConversationSetEntity != null){
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);
            if(req.getIsMute()!=null){
                imConversationSetEntity.setIsTop(req.getIsTop());
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity,queryWrapper);
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Conversation,seq);



            UpdateConversationPack pack = new UpdateConversationPack();
            pack.setConversationId(req.getConversationId());
            pack.setIsMute(req.getIsMute());
            pack.setIsTop(req.getIsTop());
            pack.setSequence(seq);
            pack.setConversationType(imConversationSetEntity.getConversationType());

            messageProducer.sendToUserExceptClient(req.getFromId(),
                    ConversationEventCommand.CONVERSATION_UPDATE,pack,
                    new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        }

        return ResponseVO.successResponse();
    }

    public ResponseVO syncConversationSet(SyncReq req) {
        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncResp<ImConversationSetEntity> resp = new SyncResp<>();
        //seq > req.getseq limit maxLimit
        QueryWrapper<ImConversationSetEntity> queryWrapper =
                new QueryWrapper<>();
        queryWrapper.eq("from_id",req.getOperater());
        queryWrapper.gt("sequence",req.getLastSequence());
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("sequence");
        List<ImConversationSetEntity> list = imConversationSetMapper
                .selectList(queryWrapper);

        if(!CollectionUtils.isEmpty(list)){
            ImConversationSetEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setDataList(list);
            //设置最大seq
            Long friendShipMaxSeq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(), req.getOperater());
            resp.setMaxSequence(friendShipMaxSeq);
            //设置是否拉取完毕
            resp.setCompleted(maxSeqEntity.getSequence() >= friendShipMaxSeq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);

    }
}

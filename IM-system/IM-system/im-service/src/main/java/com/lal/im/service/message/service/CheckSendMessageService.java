package com.lal.im.service.message.service;


import com.lal.im.common.config.AppConfig;
import com.lal.im.common.enums.*;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.service.friendship.model.entity.ImFriendShipEntity;
import com.lal.im.service.friendship.model.req.GetRelationReq;
import com.lal.im.service.friendship.service.ImFriendShipService;
import com.lal.im.service.group.model.entity.ImGroupEntity;
import com.lal.im.service.group.model.resp.GetRoleInGroupResp;
import com.lal.im.service.group.service.ImGroupMemberService;
import com.lal.im.service.group.service.ImGroupService;
import com.lal.im.service.user.model.entity.ImUserDataEntity;
import com.lal.im.service.user.service.ImUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class CheckSendMessageService {


    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendShipService imFriendShipService;

    @Autowired
    AppConfig appConfig;


    //校验用户 是否 禁用 禁言
    public ResponseVO checkSenderForvidAndMute(String fromId, Integer appId){

        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(fromId, appId);

        if(!singleUserInfo.isOk()){
            return singleUserInfo;
        }
        ImUserDataEntity user = singleUserInfo.getData();
        //用户 禁用
        if(user.getForbiddenFlag() == UserForbiddenFlagEnum.FORBIBBEN.getCode()){
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
        }
        //用户 禁言
        else if(user.getSilentFlag() == UserSilentFlagEnum.MUTE.getCode()){
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
        }


        return ResponseVO.successResponse();
    }

    //校验好友关系  有的情况不需要好友关系也可以发送消息  需要配置是否校验好友关系
    public ResponseVO checkFriendShip(String fromId,String toId,Integer appId){

        //校验关系链
        if(appConfig.isSendMessageCheckFriend()){
            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setFromId(fromId);
            fromReq.setAppId(appId);
            fromReq.setToId(toId);
            ResponseVO<ImFriendShipEntity> fromRelation = imFriendShipService.getRelation(fromReq);
            if(!fromRelation.isOk()){
                return fromRelation;
            }
            GetRelationReq toReq = new GetRelationReq();
            toReq.setFromId(toId);
            toReq.setAppId(appId);
            toReq.setToId(fromId);
            ResponseVO<ImFriendShipEntity> toRelation = imFriendShipService.getRelation(toReq);
            if(!toRelation.isOk()){
                return toRelation;
            }

            //form 把 to  删除掉了
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()!= fromRelation.getData().getStatus()){
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            //to 把 from  删除掉了
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()!= toRelation.getData().getStatus()){
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            //校验黑名单
            if(appConfig.isSendMessageCheckBlack()){
                //form 把 to  拉黑
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()!= fromRelation.getData().getBlack()){
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
                }

                //to 把 from  拉黑
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()!= toRelation.getData().getBlack()){
                    return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_BLACK_YOU);
                }
            }
        }


        return ResponseVO.successResponse();
    }

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    public ResponseVO checkGroupMessage(String fromId, String groupId,Integer appId){

        //判断用户自己是否被禁言
        ResponseVO responseVO = checkSenderForvidAndMute(fromId, appId);
        if(!responseVO.isOk()){
            return responseVO;
        }

        //判断群逻辑
        ResponseVO<ImGroupEntity> groupResp = imGroupService.getGroup(groupId, appId);
        if(!groupResp.isOk()){
            return groupResp;
        }

        //判断群成员是否在群类
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = imGroupMemberService.getRoleInGroupOne(groupId, fromId, appId);
        if(!roleInGroupOne.isOk()){
            return roleInGroupOne;
        }
        GetRoleInGroupResp currentGroupRole = roleInGroupOne.getData();

        //判断群是否被禁言
        //如果禁言 只有群管理和群主可以发言
        ImGroupEntity group = groupResp.getData();
        if(group.getMute() == GroupMuteTypeEnum.MUTE.getCode() &&
                (currentGroupRole.getRole() != GroupMemberRoleEnum.MAMAGER.getCode() ||
                        currentGroupRole.getRole() != GroupMemberRoleEnum.OWNER.getCode())){
            //当前群禁言 发消息者在群中 不是群主 或者管理员

            return ResponseVO.errorResponse(GroupErrorCode.THIS_GROUP_IS_MUTE);
        }

        if (currentGroupRole.getSpeakDate() != null && currentGroupRole.getSpeakDate()>System.currentTimeMillis()) {
            //当前角色禁言 禁言时间 >  当前时间
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }

        return ResponseVO.successResponse();

    }

}

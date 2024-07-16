package com.lal.im.service.friendship.service;


import com.lal.im.common.model.ResponseVO;
import com.lal.im.service.friendship.model.entity.ImFriendShipGroupEntity;
import com.lal.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.lal.im.service.friendship.model.req.DeleteFriendShipGroupReq;


public interface ImFriendShipGroupService {

    public ResponseVO addGroup(AddFriendShipGroupReq req);

    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

    public Long updateSeq(String fromId, String groupName, Integer appId);
}

package com.lal.im.service.friendship.service;


import com.lal.im.common.model.ResponseVO;
import com.lal.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.lal.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;


public interface ImFriendShipGroupMemberService {

    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    public int doAddGroupMember(Long groupId, String toId);

    public int clearGroupMember(Long groupId);
}

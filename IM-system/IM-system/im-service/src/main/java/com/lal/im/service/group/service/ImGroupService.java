package com.lal.im.service.group.service;


import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.SyncReq;
import com.lal.im.service.group.model.entity.ImGroupEntity;
import com.lal.im.service.group.model.req.*;

public interface ImGroupService {

    public ResponseVO importGroup(ImportGroupReq req);


    public ResponseVO createGroup(CreateGroupReq req);

    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req);

    public ResponseVO getJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO destroyGroup(DestroyGroupReq req);

    public ResponseVO transferGroup(TransferGroupReq req);

    public ResponseVO<ImGroupEntity> getGroup(String groupId, Integer appId);

    public ResponseVO getGroup(GetGroupReq req);

    public ResponseVO muteGroup(MuteGroupReq req);

    ResponseVO syncJoinedGroupList(SyncReq req);

    Long getUserGroupMaxSeq(String userId, Integer appId);
}

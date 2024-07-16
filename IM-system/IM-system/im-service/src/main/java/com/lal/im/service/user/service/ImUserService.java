package com.lal.im.service.user.service;


import com.lal.im.common.model.ResponseVO;
import com.lal.im.service.user.model.entity.ImUserDataEntity;
import com.lal.im.service.user.model.req.*;
import com.lal.im.service.user.model.resp.GetUserInfoResp;

public interface ImUserService {

    public ResponseVO importUser(ImportUserReq req);

    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req);

    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId , Integer appId);

    public ResponseVO deleteUser(DeleteUserReq req);

    public ResponseVO modifyUserInfo(ModifyUserInfoReq req);

    public ResponseVO login(LoginReq req);

    ResponseVO getUserSequence(GetUserSequenceReq req);
}

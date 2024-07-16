package com.lal.im.service.user.service;


import com.lal.im.service.user.model.UserStatusChangeNotifyContent;
import com.lal.im.service.user.model.req.PullFriendOnlineStatusReq;
import com.lal.im.service.user.model.req.PullUserOnlineStatusReq;
import com.lal.im.service.user.model.req.SetUserCustomerStatusReq;
import com.lal.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.lal.im.service.user.model.resp.UserOnlineStatusResp;

import java.util.Map;

public interface ImUserStatusService {

    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content);

    void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req);

    void setUserCustomerStatus(SetUserCustomerStatusReq req);

    Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req);

    Map<String, UserOnlineStatusResp>  queryUserOnlineStatus(PullUserOnlineStatusReq req);
}

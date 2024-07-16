package com.lal.im.service.user.controller;


import com.lal.im.common.model.ClientType;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.route.RouteHandle;
import com.lal.im.common.route.RouteInfo;
import com.lal.im.common.utils.RouteInfoParseUtil;
import com.lal.im.service.user.model.req.*;
import com.lal.im.service.user.service.ImUserService;
import com.lal.im.service.user.service.ImUserStatusService;
import com.lal.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    ImUserService imUserService;

    @Autowired
    RouteHandle routeHandle;

    @Autowired
    ZKit zKit;

    @Autowired
    ImUserStatusService imUserStatusService;

    @RequestMapping("/importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req, Integer appId){

        req.setAppId(appId);

        return imUserService.importUser(req);
    }

    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody @Validated DeleteUserReq req, Integer appId) {
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }
    /**
     * @return com.lld.im.common.ResponseVO
     * @description im的登录接口，返回im地址
     * @author chackylee
     */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req, Integer appId) {
        req.setAppId(appId);

        ResponseVO login = imUserService.login(req);

        if(login.isOk()){

            //todo  去zk获取一个im的地址 ，返回给sdk

            List<String> allNode = new ArrayList<>();
            if(req.getClientType() == ClientType.WEB.getCode()){
                allNode = zKit.getAllWebNode();
            }else {
                allNode = zKit.getAllTcpNode();
            }
            //ip:port
            String s = routeHandle.routeServer(allNode, req.getUserId());

            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }

        return ResponseVO.errorResponse();
    }

    @RequestMapping("/getUserSequence")
    public ResponseVO getUserSequence(@RequestBody GetUserSequenceReq req, Integer appId) {

        req.setAppId(appId);
        return  imUserService.getUserSequence(req);
    }

    @RequestMapping("/subscribeUserOnlineStatus")
    public ResponseVO subscribeUserOnlineStatus(@RequestBody @Validated
                                                SubscribeUserOnlineStatusReq req, Integer appId, String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        imUserStatusService.subscribeUserOnlineStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("/setUserCustomerStatus")
    public ResponseVO setUserCustomerStatus(@RequestBody @Validated
                                            SetUserCustomerStatusReq req, Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        imUserStatusService.setUserCustomerStatus(req);
        return ResponseVO.successResponse();
    }

    @RequestMapping("/queryFriendOnlineStatus")
    public ResponseVO queryFriendOnlineStatus(@RequestBody @Validated
                                              PullFriendOnlineStatusReq req, Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        return ResponseVO.successResponse(imUserStatusService.queryFriendOnlineStatus(req));
    }

    @RequestMapping("/queryUserOnlineStatus")
    public ResponseVO queryUserOnlineStatus(@RequestBody @Validated
                                            PullUserOnlineStatusReq req, Integer appId,String identifier) {
        req.setAppId(appId);
        req.setOperater(identifier);
        return ResponseVO.successResponse(imUserStatusService.queryUserOnlineStatus(req));
    }



}

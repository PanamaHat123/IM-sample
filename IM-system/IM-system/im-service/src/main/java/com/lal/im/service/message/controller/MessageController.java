package com.lal.im.service.message.controller;


import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.SyncReq;
import com.lal.im.common.model.message.CheckSendMessageReq;
import com.lal.im.service.message.model.req.SendMessageReq;
import com.lal.im.service.message.service.GroupMessageService;
import com.lal.im.service.message.service.MessageSyncService;
import com.lal.im.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 给接入方 管理员使用
 */
@RestController
@RequestMapping("/v1/message")
public class MessageController {


    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    /**
     * 接入方 管理员 发送单聊消息
     */
    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId)  {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req)  {
        return p2PMessageService.imServerPermissionCheck(req.getFromId(),req.getToId()
                ,req.getAppId());
    }

    @RequestMapping("/checkGroupSend")
    public ResponseVO checkGroupSend(@RequestBody @Validated CheckSendMessageReq req)  {
        return groupMessageService.imServerPermissionCheck(req.getFromId(),req.getToId()
                ,req.getAppId());
    }

    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody
                                         @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req);
    }



}

package com.lal.im.tcp.feign;


import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

public interface FeignMessageService {

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSendMessage(CheckSendMessageReq o);


    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/checkGroupSend")
    public ResponseVO checkGroupSendMessage(CheckSendMessageReq o);

}

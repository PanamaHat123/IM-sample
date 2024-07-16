package com.lal.im.service.utils;


import com.lal.im.common.config.AppConfig;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CallbackService {

    @Autowired
    HttpRequestUtils httpRequestUtils;

    @Autowired
    AppConfig appConfig;

    @Autowired
    ShareThreadPool shareThreadPool;


    //之后回调
    public void callback(Integer appId, String callbackCommand, String jsonBody) {
        shareThreadPool.submit(() -> {
            try {
                httpRequestUtils.doPost("", Object.class, builderUrlParams(appId, callbackCommand), jsonBody, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    //之前回调
    public ResponseVO beforeCallback(Integer appId, String callbackCommand, String jsonBody) {
        try {
            ResponseVO responseVO = httpRequestUtils.doPost(appConfig.getCallbackUrl(), ResponseVO.class, builderUrlParams(appId, callbackCommand),
                    jsonBody, null);
            return responseVO;
        } catch (Exception e) {
            log.error("callback 之前 回调{} : {}出现异常 ： {} ", callbackCommand, appId, e.getMessage());
            return ResponseVO.successResponse();
        }
    }


    public Map builderUrlParams(Integer appId, String command) {
        Map map = new HashMap();
        map.put("appId", appId);
        map.put("command", command);
        return map;
    }

}

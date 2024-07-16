package com.lal.im.service.utils;


import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ImConnectStatusEnum;
import com.lal.im.common.model.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class UserSessionUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //1. 获取用户所有的session
    public List<UserSession> getUserSession(Integer appId, String userId){

        String userSessionKey = appId+ Constants.RedisConstants.UserSessionConstants+userId;

        Map<Object, Object> entries =
                stringRedisTemplate.opsForHash().entries(userSessionKey);

        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object value : values) {
            String str = (String) value;
            UserSession userSession = JSONObject.parseObject(str,UserSession.class);

            if(userSession.getConnectState() == ImConnectStatusEnum.ONLINE_STATUS.getCode()){
                // 在线状态
                list.add(userSession);
            }

        }
        return list;
    }


    //2. 获取用户除了本端的session


    //1. 获取指定端的 session
    public UserSession getUserSession(Integer appId,String userId
            ,Integer clientType,String imei){

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants
                + userId;
        String hashKey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hashKey);
        UserSession session =
                JSONObject.parseObject(o.toString(), UserSession.class);
        return session;
    }


}

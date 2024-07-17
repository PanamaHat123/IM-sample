package com.lal.im.tcp.redis;

import com.lal.im.common.codec.config.BootstrapConfig;
import com.lal.im.tcp.reciver.UserLoginMessageListener;
import lombok.Data;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Data
@Component
public class RedisManager {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    BootstrapConfig config;

    private Integer loginModel;

    @PostConstruct
    public void init(){
        loginModel = config.getLoginModel();
        UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel,redissonClient);
        userLoginMessageListener.listenerUserLogin();
    }



}

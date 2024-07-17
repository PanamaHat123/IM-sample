package com.lal.im.common.codec.config;


import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnProperty(prefix = "tim",name = "tcpPort")
@ConfigurationProperties(prefix = "tim")
public class BootstrapConfig {


    private Integer tcpPort;

    private Integer webSocketPort;

    private boolean tcpEnabled;

    private boolean webSocketEnabled;

    private Integer bossThreadSize;

    private Integer workThreadSize;

    private Long heartBeatTime; // 心跳超时时间  毫秒

    private Integer brokerId; //区分服务

    private Integer loginModel;

    private String logicUrl;


}

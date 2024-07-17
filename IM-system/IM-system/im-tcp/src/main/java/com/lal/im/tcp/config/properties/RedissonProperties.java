package com.lal.im.tcp.config.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    private String address;
    private String password;
    private Integer timeout;
    private Integer database;
    private Integer connectionPoolSize;
    private Integer connectionMinimumIdleSize;
    private Integer poolMinIdle;
    private Integer poolConnTimeout;
    private Integer poolSize;

}

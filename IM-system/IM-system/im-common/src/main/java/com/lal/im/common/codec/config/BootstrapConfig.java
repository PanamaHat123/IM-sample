package com.lal.im.common.codec.config;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class BootstrapConfig {

    private TcpConfig lim;

    @Data
    public static class TcpConfig{

        private Integer tcpPort;

        private Integer webSocketPort;

        private Integer bossThreadSize;

        private Integer workThreadSize;

        private Long heartBeatTime; // 心跳超时时间  毫秒

        private Integer brokerId; //区分服务

        private Integer loginModel;

        private String  logicUrl;

        private RedisConfig redis;

        private Rabbitmq rabbitmq;

        private ZkConfig zkConfig;


    }

    @Data
    public static class ZkConfig {
        /**
         * zk连接地址
         */
        private String zkAddr;

        /**
         * zk连接超时时间
         */
        private Integer zkConnectTimeOut;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisConfig {

        /**
         * 单机模式：single 哨兵模式：sentinel 集群模式：cluster
         */
        private String mode;
        /**
         * 数据库
         */
        private Integer database;
        /**
         * 密码
         */
        private String password;
        /**
         * 超时时间
         */
        private Integer timeout;
        /**
         * 最小空闲数
         */
        private Integer poolMinIdle;
        /**
         * 连接超时时间(毫秒)
         */
        private Integer poolConnTimeout;
        /**
         * 连接池大小
         */
        private Integer poolSize;

        /**
         * redis单机配置
         */
        private RedisSingle single;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisSingle {
        /**
         * 地址
         */
        private String address;
    }


    /**
     * rabbitmq哨兵模式配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rabbitmq {

        private String host;

        private Integer port;

        private String virtualHost;

        private String userName;

        private String password;
    }


}

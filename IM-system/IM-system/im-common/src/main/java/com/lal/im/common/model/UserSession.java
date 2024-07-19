package com.lal.im.common.model;

import lombok.Data;


@Data
public class UserSession {

    private String userId;

    /**
     * appid
     */
    private Integer appId;

    /**
     * iso android web
     */
    private Integer clientType;

    //sdk version
    private Integer version;

    // 1=online 2=offline
    private Integer connectState;

    private Integer brokerId;

    private String brokerHost;

    private String imei;

}

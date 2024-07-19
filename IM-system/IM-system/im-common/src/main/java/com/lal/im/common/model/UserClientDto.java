package com.lal.im.common.model;

import lombok.Data;


@Data
public class UserClientDto {

    private Integer appId;

    private Integer clientType;

    private String userId;

    private String imei;

}

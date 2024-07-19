package com.lal.im.common.codec.pack.user;

import com.lal.im.common.model.UserSession;
import lombok.Data;

import java.util.List;


@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}

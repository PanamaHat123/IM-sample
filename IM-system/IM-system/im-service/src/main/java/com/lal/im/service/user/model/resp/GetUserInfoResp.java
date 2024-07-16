package com.lal.im.service.user.model.resp;

import com.lal.im.service.user.model.entity.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class GetUserInfoResp {

    private List<ImUserDataEntity> userDataItem;

    private List<String> failUser;


}

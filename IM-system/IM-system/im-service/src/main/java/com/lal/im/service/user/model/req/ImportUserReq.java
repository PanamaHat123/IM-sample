package com.lal.im.service.user.model.req;


import com.lal.im.common.model.RequestBase;
import com.lal.im.service.user.model.entity.ImUserDataEntity;
import lombok.Data;

import java.util.List;

@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;

}

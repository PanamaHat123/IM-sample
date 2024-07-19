package com.lal.im.service.user.model.req;

import com.lal.im.common.model.RequestBase;
import lombok.Data;


@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}

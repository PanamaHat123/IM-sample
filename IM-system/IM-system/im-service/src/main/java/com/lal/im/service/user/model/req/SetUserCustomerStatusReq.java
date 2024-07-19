package com.lal.im.service.user.model.req;

import com.lal.im.common.model.RequestBase;
import lombok.Data;


@Data
public class SetUserCustomerStatusReq extends RequestBase {

    private String userId;

    private String customText;

    private Integer customStatus;

}

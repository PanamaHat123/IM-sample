package com.lal.im.service.user.model.req;

import com.lal.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class GetUserSequenceReq extends RequestBase {

    private String userId;

}

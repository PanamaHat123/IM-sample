package com.lal.im.service.group.model.req;

import com.lal.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class TransferGroupReq extends RequestBase {

    @NotNull(message = "群id不能为空")
    private String groupId;

    private String ownerId;

}

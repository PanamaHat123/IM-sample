package com.lal.im.service.group.model.callback;

import com.lal.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;


@Data
public class AddMemberAfterCallback {
    private String groupId;
    private Integer groupType;
    private String operater;
    private List<AddMemberResp> memberId;
}

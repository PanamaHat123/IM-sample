package com.lal.im.common.model.message;

import lombok.Data;

import java.util.List;


@Data
public class GroupChatMessageContent extends MessageContent {

    private String groupId;

    private List<String> memberId;

}

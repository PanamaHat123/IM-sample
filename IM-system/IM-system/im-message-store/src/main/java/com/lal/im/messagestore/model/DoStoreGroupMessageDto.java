package com.lal.im.messagestore.model;

import com.lal.im.common.model.message.GroupChatMessageContent;
import com.lal.im.messagestore.model.entity.ImMessageBodyEntity;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class DoStoreGroupMessageDto {

    private GroupChatMessageContent groupChatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}

package com.lal.im.messagestore.model;

import com.lal.im.common.model.message.MessageContent;
import com.lal.im.messagestore.model.entity.ImMessageBodyEntity;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}

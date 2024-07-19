package com.lal.im.common.model.message;

import lombok.Data;


@Data
public class DoStoreP2PMessageDto {

    private MessageContent messageContent;

    private ImMessageBody messageBody;

}

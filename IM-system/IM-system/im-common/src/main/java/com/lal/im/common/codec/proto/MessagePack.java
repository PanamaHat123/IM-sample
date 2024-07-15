package com.lal.im.common.codec.proto;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: The message service sends a packet body to TCP,
 * which then parses the packet body into a Message and sends it to the client.
 **/
@Data
public class MessagePack<T> implements Serializable {

    private String userId;

    private Integer appId;

    /**
     * receiver
     */
    private String toId;


    private int clientType;


    private String messageId;

    /**
     * Client device unique identifier.
     */
    private String imei;

    private Integer command;

    /**
     * Business data object; if it is a chat message, no parsing is needed, and it is transmitted directly as-is.
     */
    private T data;

//    private String userSign;

}

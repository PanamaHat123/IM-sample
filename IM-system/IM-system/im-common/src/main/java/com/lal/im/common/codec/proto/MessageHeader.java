package com.lal.im.common.codec.proto;

import lombok.Data;

@Data
public class MessageHeader {

    //Message operation command in hexadecimal; the beginning of a message is typically denoted by 0x.
    //4 Bytes
    private Integer command;
    //4 Bytes
    private Integer version;
    //4 Bytes
    private Integer clientType;
    //4 Bytes
    private Integer appId;
    /**
     * Data parsing type, unrelated to specific business. Subsequent parsing of the data will be based on the parsing type.
     * 0x0: JSON,
     * 0x1: ProtoBuf,
     * 0x2: XML,
     * default: 0x0.
     */
    //4 Bytes Parsing type
    private Integer messageType = 0x0;

    //4 Bytes
    private Integer imeiLength;

    //4 Bytes body length
    private int length;

    //imei
    private String imei;

}

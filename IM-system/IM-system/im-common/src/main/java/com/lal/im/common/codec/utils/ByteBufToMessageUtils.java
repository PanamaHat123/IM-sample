package com.lal.im.common.codec.utils;

import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.codec.proto.Message;
import com.lal.im.common.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;

/**
 * @description:
 * Convert ByteBuf to a Message entity, based on the private protocol conversion. Private protocol rules:
 * 4 bytes represent Command, indicating the start of a message,
 * 4 bytes represent Version,
 * 4 bytes represent ClientType,
 * 4 bytes represent MessageType,
 * 4 bytes represent AppId,
 * 4 bytes represent the length of IMEI,
 * imei
 * 4 bytes represent the data length,
 * data
 * Subsequently, the decoding method will be added to the data header to decode based on different decoding methods, such as PB, JSON. Currently, JSON strings are used.
 */
public class ByteBufToMessageUtils {

    public static Message transition(ByteBuf in){

        /** command*/
        int command = in.readInt();

        /** version*/
        int version = in.readInt();

        /** clientType*/
        int clientType = in.readInt();

        /** clientType*/
        int messageType = in.readInt();

        /** appId*/
        int appId = in.readInt();

        /** imeiLength*/
        int imeiLength = in.readInt();

        /** bodyLen*/
        int bodyLen = in.readInt();

        if(in.readableBytes() < bodyLen + imeiLength){
            in.resetReaderIndex();
            return null;
        }

        byte [] imeiData = new byte[imeiLength];
        in.readBytes(imeiData);
        String imei = new String(imeiData);

        byte [] bodyData = new byte[bodyLen];
        in.readBytes(bodyData);


        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setClientType(clientType);
        messageHeader.setCommand(command);
        messageHeader.setLength(bodyLen);
        messageHeader.setVersion(version);
        messageHeader.setMessageType(messageType);
        messageHeader.setImei(imei);
        messageHeader.setImeiLength(imeiLength);

        Message message = new Message();
        message.setMessageHeader(messageHeader);

        if(messageType == 0x0){
            String body = new String(bodyData);
            JSONObject parse = (JSONObject) JSONObject.parse(body);
            message.setMessagePack(parse);
        }else{

        }

        in.markReaderIndex();
        return message;
    }

}

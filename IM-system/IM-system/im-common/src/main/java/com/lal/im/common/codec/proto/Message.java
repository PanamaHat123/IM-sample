package com.lal.im.common.codec.proto;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class Message {

    private MessageHeader messageHeader;

    private Object messagePack;


    public <T> T getMessagePack(Class<T> clazz) {
        T o = JSON.parseObject(JSONObject.toJSONString(this.getMessagePack()), clazz);
        return o;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messagePack=" + messagePack +
                '}';
    }
}

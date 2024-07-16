package com.lal.im.tcp.utils;

import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.codec.pack.user.UserStatusChangeNotifyPack;
import com.lal.im.common.codec.proto.MessageHeader;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ImConnectStatusEnum;
import com.lal.im.common.enums.command.UserEventCommand;
import com.lal.im.common.model.UserClientDto;
import com.lal.im.common.model.UserSession;
import com.lal.im.tcp.publish.MqMessageProducer;
import com.lal.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionSocketHolder {

    private static final Map<UserClientDto, NioSocketChannel> CHANNELS = new ConcurrentHashMap<>();

    public static void put(Integer appId, String userId,Integer clientType,String imei, NioSocketChannel channel){

        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        dto.setImei(imei);
        CHANNELS.put(dto,channel);
    }


    public static NioSocketChannel get(Integer appId,String userId,Integer clientType,String imei){
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        dto.setImei(imei);

        return CHANNELS.get(dto);
    }

    public static void remove(Integer appId,String userId,Integer clientType,String imei){
        UserClientDto dto = new UserClientDto();
        dto.setAppId(appId);
        dto.setUserId(userId);
        dto.setClientType(clientType);
        dto.setImei(imei);

        CHANNELS.remove(dto);
    }

    public static void remove(NioSocketChannel channel){

        CHANNELS.entrySet().stream().filter(entity->entity.getValue() == channel).forEach(entry->CHANNELS.remove(entry.getKey()));
    }

    public static void removeUserSession(NioSocketChannel nioSocketChannel){
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

        SessionSocketHolder.remove(appId,userId,clientType,imei);
        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId +
                Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientType+":"+imei);

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setClientType(clientType);

        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        userStatusChangeNotifyPack.setAppId(appId);
        userStatusChangeNotifyPack.setUserId(userId);
        userStatusChangeNotifyPack.setStatus(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
        MqMessageProducer.sendMessage(messageHeader,messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

        nioSocketChannel.close();

    }

    public static void offlineUserSession(NioSocketChannel nioSocketChannel){
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();


        SessionSocketHolder.remove(appId,userId,clientType,imei);
        RedissonClient redissonClient = RedisManager.getRedissonClient();

        RMap<String, String> map = redissonClient.getMap(appId +
                Constants.RedisConstants.UserSessionConstants + userId);
        String sessionStr = map.get(clientType.toString()+":"+imei);

        if(!StringUtils.isBlank(sessionStr)){
            UserSession userSession = JSONObject.parseObject(sessionStr, UserSession.class);
            userSession.setConnectState(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
            map.put(clientType+":"+imei,JSONObject.toJSONString(userSession));
        }

        MessageHeader messageHeader = new MessageHeader();
        messageHeader.setAppId(appId);
        messageHeader.setImei(imei);
        messageHeader.setClientType(clientType);

        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        userStatusChangeNotifyPack.setAppId(appId);
        userStatusChangeNotifyPack.setUserId(userId);
        userStatusChangeNotifyPack.setStatus(ImConnectStatusEnum.OFFLINE_STATUS.getCode());
        MqMessageProducer.sendMessage(messageHeader,messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

        nioSocketChannel.close();
    }

    public static List<NioSocketChannel> get(Integer appId , String id) {

        Set<UserClientDto> channelInfos = CHANNELS.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(channel ->{
            if(channel.getAppId().equals(appId) && id.equals(channel.getUserId())){
                channels.add(CHANNELS.get(channel));
            }
        });

        return channels;
    }



}

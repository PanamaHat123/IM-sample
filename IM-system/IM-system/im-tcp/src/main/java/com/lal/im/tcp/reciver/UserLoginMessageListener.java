package com.lal.im.tcp.reciver;


import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.codec.proto.MessagePack;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.DeviceMultiLoginEnum;
import com.lal.im.common.enums.command.SystemCommand;
import com.lal.im.common.model.ClientType;
import com.lal.im.common.model.UserClientDto;
import com.lal.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.List;

/**
 * 多端同步：  1 单端登录 ： 一端在线 踢掉除本 clientType + imei 的设备
 *            2 双端登录  允许 pc/mobile  其中一端登录  + web端  踢掉除本 clientType + imei 的web设备
 *            3 三端登录  允许 手机+ pc + web  提交通同端的其他imei 出web
 *            4 多端登录  不做任何处理
 *
 */


@Slf4j
public class UserLoginMessageListener {

    private Integer loginModel;

    RedissonClient redissonClient;

    public UserLoginMessageListener(Integer loginModel,RedissonClient redissonClient) {
        this.loginModel = loginModel;
        this.redissonClient = redissonClient;
    }

    public void listenerUserLogin(){

        RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);

        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String msg) {
                log.info("收到用户上线通知：{}",msg);
                UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);
                //获取当前服务器 登录了的 用户channel
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());

                for (NioSocketChannel channel : nioSocketChannels) {
                    // channel的信息
                    Integer clientType = (Integer) channel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                    String imei = (String) channel.attr(AttributeKey.valueOf(Constants.Imei)).get();
                    String channelClientTypeImei = clientType+":"+imei;

                    if(loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()){


                        if(!channelClientTypeImei.equals(dto.getClientType()+":"+dto.getImei())){
                            // todo 踢掉客户端

                            //告诉客户端  其他端登录
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            channel.writeAndFlush(pack);
                        }


                    }else if(loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()){

                        if(dto.getClientType() == ClientType.WEB.getCode()){
                            //web 端 不处理
                            continue;
                        }
                        if(clientType == ClientType.WEB.getCode()){
                            continue;
                        }
                        if(!channelClientTypeImei.equals(dto.getClientType()+":"+dto.getImei())){
                            // todo 踢掉客户端
                            //告诉客户端  其他端登录
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            channel.writeAndFlush(pack);

                        }

                    }else if(loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()){

                        if(dto.getClientType() == ClientType.WEB.getCode()){
                            //web 端 不处理
                            continue;
                        }

                        Boolean isSameClient = false;
                        if((clientType == ClientType.IOS.getCode() || clientType == ClientType.ANDROID.getCode())
                          && (dto.getClientType() ==ClientType.IOS.getCode() || dto.getClientType() ==ClientType.ANDROID.getCode()) ){
                            // 之前的channel 是ios或者android 并且 当前登录的端也是 ios或者android
                            isSameClient = true;
                        }
                        if((clientType == ClientType.MAC.getCode() || clientType == ClientType.WINDOWS.getCode())
                                && (dto.getClientType() ==ClientType.MAC.getCode() || dto.getClientType() ==ClientType.WINDOWS.getCode()) ){
                            // 之前的channel 是MAC或者windows 并且 当前登录的端也是 MAC或者windows
                            isSameClient = true;
                        }

                        if(isSameClient && !channelClientTypeImei.equals(dto.getClientType()+":"+dto.getImei())){
                            //同手机（pc） 并且imei号不同
                            // todo 踢掉客户端
                            //告诉客户端  其他端登录
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) channel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) channel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            channel.writeAndFlush(pack);
                        }

                    }


                }



            }
        });

    }

}

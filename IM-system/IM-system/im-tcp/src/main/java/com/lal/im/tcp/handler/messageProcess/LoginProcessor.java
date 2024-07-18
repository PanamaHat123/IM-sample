package com.lal.im.tcp.handler.messageProcess;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSONObject;
import com.lal.im.common.codec.pack.LoginPack;
import com.lal.im.common.codec.pack.user.LoginAckPack;
import com.lal.im.common.codec.pack.user.UserStatusChangeNotifyPack;
import com.lal.im.common.codec.proto.Message;
import com.lal.im.common.codec.proto.MessageHeader;
import com.lal.im.common.codec.proto.MessagePack;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ImConnectStatusEnum;
import com.lal.im.common.enums.command.Command;
import com.lal.im.common.enums.command.MessageCommand;
import com.lal.im.common.enums.command.SystemCommand;
import com.lal.im.common.enums.command.UserEventCommand;
import com.lal.im.common.model.UserClientDto;
import com.lal.im.common.model.UserSession;
import com.lal.im.tcp.publish.MqMessageProducer;
import com.lal.im.tcp.redis.RedisManager;
import com.lal.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;

@Service
public class LoginProcessor implements IMessageProcessor{

    Command command = SystemCommand.LOGIN;

    @Autowired
    RedisManager redisManager;

    @Value("${tim.brokerId}")
    Integer brokerId;

    @Override
    public Command getCommand() {
        return  command;
    }

    @Override
    public void process(ChannelHandlerContext ctx, Message msg) {

        // RedisManager redisManager = SpringUtil.getBean(RedisManager.class);

        MessageHeader messageHeader = msg.getMessageHeader();
        LoginPack loginPack = msg.getMessagePack(LoginPack.class);

        ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
        ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(messageHeader.getAppId());
        ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(messageHeader.getClientType());
        ctx.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(messageHeader.getImei());


        //Redis map
        UserSession userSession = new UserSession();
        userSession.setAppId(messageHeader.getAppId());
        userSession.setClientType(messageHeader.getClientType());
        userSession.setUserId(loginPack.getUserId());
        userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
        userSession.setBrokerId(brokerId);
        userSession.setImei(messageHeader.getImei());
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            userSession.setBrokerHost(localHost.getHostAddress());
        }catch (Exception e){
            e.printStackTrace();
        }

        // save in redis
        RedissonClient redissonClient = redisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(messageHeader.getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
        map.put(messageHeader.getClientType()+":"+messageHeader.getImei(), JSONObject.toJSONString(userSession));

        // save channel
        SessionSocketHolder.put(messageHeader.getAppId(),
                loginPack.getUserId(),
                messageHeader.getClientType(),messageHeader.getImei(),
                (NioSocketChannel) ctx.channel());

        //通知其他服务该用户登录了  其他服务收到通知  根据多端登录策略 判断是否需要下线
        UserClientDto dto = new UserClientDto();
        dto.setImei(messageHeader.getImei());
        dto.setUserId(loginPack.getUserId());
        dto.setClientType(messageHeader.getClientType());
        dto.setAppId(messageHeader.getAppId());
        RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);// redis 发起通知
        topic.publish(JSONObject.toJSONString(dto));//发送消息

        //上线状态通知
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        userStatusChangeNotifyPack.setAppId(messageHeader.getAppId());
        userStatusChangeNotifyPack.setUserId(loginPack.getUserId());
        userStatusChangeNotifyPack.setStatus(ImConnectStatusEnum.ONLINE_STATUS.getCode());
        //发送给 mq
        MqMessageProducer.sendMessage(userStatusChangeNotifyPack,messageHeader, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());


        // 告诉用户已经登录成功\ ack
        MessagePack<LoginAckPack> loginSuccess = new MessagePack<>();
        LoginAckPack loginAckPack = new LoginAckPack();
        loginAckPack.setUserId(loginAckPack.getUserId());
        loginSuccess.setCommand(SystemCommand.LOGINACK.getCommand());
        loginSuccess.setData(loginAckPack);
        loginSuccess.setImei(messageHeader.getImei());
        loginSuccess.setAppId(messageHeader.getAppId());
        ctx.channel().writeAndFlush(loginSuccess);

    }
}

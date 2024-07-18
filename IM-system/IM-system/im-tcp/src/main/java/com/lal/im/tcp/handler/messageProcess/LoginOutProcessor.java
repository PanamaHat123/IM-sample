package com.lal.im.tcp.handler.messageProcess;

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
public class LoginOutProcessor implements IMessageProcessor{

    Command command = SystemCommand.LOGOUT;

    @Override
    public Command getCommand() {
        return command;
    }

    @Override
    public void process(ChannelHandlerContext ctx, Message msg) {
        //退出登录
        //删除session -> channel
        //删除redis里的登录信息 当前退出端
        SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
    }
}

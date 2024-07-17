package com.lal.im.tcp.handler;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lal.im.common.codec.pack.LoginPack;
import com.lal.im.common.codec.pack.message.ChatMessageAck;
import com.lal.im.common.codec.pack.user.LoginAckPack;
import com.lal.im.common.codec.pack.user.UserStatusChangeNotifyPack;
import com.lal.im.common.codec.proto.Message;
import com.lal.im.common.codec.proto.MessageHeader;
import com.lal.im.common.codec.proto.MessagePack;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.enums.ImConnectStatusEnum;
import com.lal.im.common.enums.command.GroupEventCommand;
import com.lal.im.common.enums.command.MessageCommand;
import com.lal.im.common.enums.command.SystemCommand;
import com.lal.im.common.enums.command.UserEventCommand;
import com.lal.im.common.model.ResponseVO;
import com.lal.im.common.model.UserClientDto;
import com.lal.im.common.model.UserSession;
import com.lal.im.common.model.message.CheckSendMessageReq;
import com.lal.im.tcp.feign.FeignMessageService;
import com.lal.im.tcp.publish.MqMessageProducer;
import com.lal.im.tcp.redis.RedisManager;
import com.lal.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private Integer brokerId;

    private FeignMessageService feignMessageService;

    private String logicUrl;

    public NettyServerHandler(Integer brokerId,String logicUrl) {
        this.brokerId = brokerId;
        feignMessageService = Feign.builder().encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000,3500)) // 设置超时时间
                .target(FeignMessageService.class,logicUrl);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        RedisManager redisManager = SpringUtil.getBean(RedisManager.class);

        MessageHeader messageHeader = msg.getMessageHeader();
        Integer command = messageHeader.getCommand();

        // 登录command
        if(command == SystemCommand.LOGIN.getCommand()){

            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()), new TypeReference<LoginPack>() {
            }.getType());

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

            //存到redis
            RedissonClient redissonClient = redisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(messageHeader.getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(messageHeader.getClientType()+":"+messageHeader.getImei(),JSONObject.toJSONString(userSession));

            // 将channel 存起来
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

        }else if(command == SystemCommand.LOGOUT.getCommand()){
            //退出登录
            //删除session -> channel
            //删除redis里的登录信息 当前退出端
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());


        }else if (command == SystemCommand.PING.getCommand()){
            //心跳检测
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());

        }
        else if (command == MessageCommand.MSG_P2P.getCommand()
                || command == GroupEventCommand.MSG_GROUP.getCommand()){

            try {
                //单聊处理
                //todo 1.调用校验消息发送发的接口
                ResponseVO responseVO =null;
                CheckSendMessageReq req = new CheckSendMessageReq();
                req.setAppId(messageHeader.getAppId());
                req.setCommand(messageHeader.getCommand());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
                req.setFromId(jsonObject.getString("fromId"));

                //群聊/单聊  校验的接口不同
                if(command == MessageCommand.MSG_P2P.getCommand()){
                    req.setToId(jsonObject.getString("toId"));
                    responseVO =  feignMessageService.checkSendMessage(req);
                }else{
                    req.setToId(jsonObject.getString("groupId"));
                    responseVO =  feignMessageService.checkGroupSendMessage(req);
                }

                if(responseVO.isOk()){
                    //如果成功 投递到mq
                    //消息发送给 逻辑层 tomcat
                    MqMessageProducer.sendMessage(msg,command);
                }else{

                    //失败则 直接ack
                    Integer ackCommand = 0;
                    //群聊/单聊
                    if(command == MessageCommand.MSG_P2P.getCommand()){
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    }else{
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }

                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    responseVO.setData(chatMessageAck);
                    MessagePack<ResponseVO> ack = new MessagePack<>();
                    ack.setData(responseVO);
                    ack.setCommand(ackCommand);
                    ctx.channel().writeAndFlush(ack);
                }


            }catch (Exception e){

            }


        }else{
            //消息发送给 逻辑层 tomcat
            MqMessageProducer.sendMessage(msg,command);

        }


    }

    //表示 channel 处于不活动状态
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

}

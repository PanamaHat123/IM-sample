package com.lal.im.tcp;

import com.lal.im.common.codec.config.BootstrapConfig;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.utils.register.IRegisterCenter;
import com.lal.im.tcp.reciver.MessageReciver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class TcpApplicationListener implements ApplicationListener<ContextRefreshedEvent> {


    @Autowired(required = false)
    IRegisterCenter registerCenter;

    @Value("${tim.tcpPort}")
    Integer tcpPort;

    @Value("${tim.tcpEnabled}")
    Boolean tcpEnabled;

    @Value("${tim.webSocketPort}")
    Integer webSocketPort;

    @Value("${tim.webSocketEnabled}")
    Boolean webSocketEnabled;

    @Autowired
    BootstrapConfig bootstrapConfig;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String hostAddress = null;
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
            if (tcpEnabled) {
                registerCenter.register(Constants.NacosTcpServerName, hostAddress, tcpPort);
            }
            if (webSocketEnabled) {
                registerCenter.register(Constants.NacosWsServerName, hostAddress, webSocketPort);
            }
            MessageReciver.init(bootstrapConfig.getBrokerId()+"");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

}

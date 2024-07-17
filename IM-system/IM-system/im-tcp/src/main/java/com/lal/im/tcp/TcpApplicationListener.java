package com.lal.im.tcp;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.lal.im.common.codec.config.BootstrapConfig;
import com.lal.im.common.constant.Constants;
import com.lal.im.common.utils.register.IRegisterCenter;
import com.lal.im.tcp.reciver.MessageReciver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
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
                registerCenter.register(Constants.NacosWsServerName, hostAddress, tcpPort);
            }
            MessageReciver.init(bootstrapConfig.getBrokerId()+"");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

}

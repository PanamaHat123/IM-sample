package com.lal.im.tcp;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
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

    @Autowired
    private NacosServiceManager nacosServiceManager;



    @Value("${tim.tcpServerName}")
    String tcpServerName;

    @Value("${tim.webSocketServerName}")
    String webSocketServerName;

    @Value("${tim.tcpPort}")
    Integer tcpPort;

    @Value("${tim.tcpEnabled}")
    Boolean tcpEnabled;

    @Value("${tim.webSocketPort}")
    Integer webSocketPort;

    @Value("${tim.webSocketEnabled}")
    Boolean webSocketEnabled;



    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        NamingService namingService = nacosServiceManager.getNamingService();
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            if(tcpEnabled){
                namingService.registerInstance(tcpServerName,hostAddress ,tcpPort);
            }
            if(webSocketEnabled){
                namingService.registerInstance(webSocketServerName,hostAddress ,webSocketPort);
            }

        } catch (NacosException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

    }

}

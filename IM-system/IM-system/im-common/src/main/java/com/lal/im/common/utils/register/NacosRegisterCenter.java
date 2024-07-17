package com.lal.im.common.utils.register;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@Slf4j
@ConditionalOnClass(NacosServiceManager.class)
public class NacosRegisterCenter implements IRegisterCenter{

    @Autowired(required = false)
    private NacosServiceManager nacosServiceManager;

    @Override
    public void register(String serverName, String ip, Integer port) {
        NamingService namingService = nacosServiceManager.getNamingService();
        try {
            namingService.registerInstance(serverName,ip,port);
        } catch (NacosException e) {
            log.error("register server to nacos failed,{}",e.getErrMsg());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(String serverName, String groupName, String ip, Integer port) {
        NamingService namingService = nacosServiceManager.getNamingService();
        try {
            namingService.registerInstance(serverName,groupName,ip,port);
        } catch (NacosException e) {
            log.error("register server to nacos failed,{}",e.getErrMsg());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAllServerByName(String serverName) {
        NamingService namingService = nacosServiceManager.getNamingService();
        try {
            List<Instance> allInstances = namingService.getAllInstances(serverName);
            List<String> ipPortList = allInstances.stream().map(instance -> {
                String ip = instance.getIp();
                int port = instance.getPort();
                return ip + ":" + port;
            }).collect(Collectors.toList());
            return ipPortList;
        } catch (NacosException e) {
            log.error("get all server info from nacos failed,{}",e.getErrMsg());
            throw new RuntimeException(e);
        }
    }
}

package com.lal.im.common.utils.register;

import java.util.List;

public interface IRegisterCenter {

    void register(String serverName,String ip,Integer port);

    void register(String serverName,String groupName,String ip,Integer port);

    List<String> getAllServerByName(String serverName);

}

package com.lal.im.tcp.test;


import com.lal.im.tcp.TcpApplication;
import com.lal.im.tcp.test.mq.MessageReciver;
import com.lal.im.tcp.test.mq.MqFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;



public class MqTest {

    public static void main(String[] args) {
        MqFactory.init();
        MessageReciver.init("channel1","test1","test","1");
        MessageReciver.init("channel2","test2","test","2");
        MessageReciver.init("channel2","test2","test","3");

    }

}

package com.lal.im.tcp;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Map;

@Slf4j
@SpringBootApplication(scanBasePackages = {
        "com.lal.im.common", "com.lal.im.tcp"
})
public class TcpApplication {


    static String port;

    @Autowired(required = false)
    @Value("${server.port}")
    public void setPort(String port) {
        TcpApplication.port = port;
    }


    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(TcpApplication.class, args);
        log.info("spring boot server is running on port {}...",port);
    }
}
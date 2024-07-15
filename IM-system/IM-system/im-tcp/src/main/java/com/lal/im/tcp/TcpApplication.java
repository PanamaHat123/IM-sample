package com.lal.im.tcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@Slf4j
@SpringBootApplication
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
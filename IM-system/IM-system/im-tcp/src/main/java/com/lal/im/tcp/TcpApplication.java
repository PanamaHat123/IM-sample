package com.lal.im.tcp;

import com.lal.im.tcp.server.TimServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class TcpApplication {
    public static void main(String[] args) {

        SpringApplication.run(TcpApplication.class,args);
    }
}
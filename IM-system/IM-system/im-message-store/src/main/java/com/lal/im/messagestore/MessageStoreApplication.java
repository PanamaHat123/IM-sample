package com.lal.im.messagestore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lal.im.messagestore.mapper")
public class MessageStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageStoreApplication.class, args);
    }


}



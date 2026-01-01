package com.example.msarrosage;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@EnableDiscoveryClient
@EnableRabbit
@EnableScheduling
@SpringBootApplication
public class MsArrosageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsArrosageApplication.class, args);
    }
}

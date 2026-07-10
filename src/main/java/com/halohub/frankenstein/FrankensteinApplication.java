package com.halohub.frankenstein;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FrankensteinApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrankensteinApplication.class, args);
    }

}

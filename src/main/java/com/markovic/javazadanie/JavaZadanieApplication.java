package com.markovic.javazadanie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.markovic.javazadanie")
public class JavaZadanieApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaZadanieApplication.class, args);
    }

}

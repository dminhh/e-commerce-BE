package com.infor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.common.*",
        "com.infor.*"
})
public class InformationApplication {
    public static void main(String[] args) {
        SpringApplication.run(InformationApplication.class, args);
    }
}
package com.ecommerce.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ecommerce.app"})  // ✅ FORCE LE SCAN
public class EcommerceAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceAppApplication.class, args);
    }
}
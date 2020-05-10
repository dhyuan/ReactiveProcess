package com.ech.kitchen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class KitchenApp {

    public static void main(final String[] args) throws IOException {
        SpringApplication.run(KitchenApp.class, args);
    }
}

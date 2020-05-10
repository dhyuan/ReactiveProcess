package com.ech.kitchen;

import com.ech.kitchen.impl.KitchenSystem;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class KitchenTestApp {

    private static final Logger LOG = LogManager.getLogger(KitchenTestApp.class);


//    @Autowired
//    public IOrderScanner orderScanner;
//
//    @Autowired
//    public IOrderObserver kitchenOrderReceiver;
//
//    @Autowired
//    public IKitchenSystem kitchenSystem;


//        @Bean
//        public CommandLineRunner commandLineRunner(final ApplicationContext ctx) {
//            return args -> {
//                kitchenOrderReceiver.beginObserve();
//                kitchenOrderReceiver.nextOrder();
//                kitchenSystem.closeKitchen();
//            };
//        }

    public static void main(final String[] args) throws IOException {
        SpringApplication.run(KitchenTestApp.class, args);
    }

}
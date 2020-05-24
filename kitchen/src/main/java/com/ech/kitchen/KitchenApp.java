package com.ech.kitchen;

import com.ech.kitchen.service.ICourierService;
import com.ech.kitchen.service.IKitchenService;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.impl.OrderFileScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.util.concurrent.Semaphore;

@SpringBootApplication
@PropertySource("classpath:/application.properties")
@Slf4j
public class KitchenApp {

    @Value("${order.file.name:order.json}")
    private String orderFileName;

    @Value("${order.ingestion.rate:100}")
    private long ingestionRate;

    @Bean
    public IOrderScanner orderScanner() {
        final IOrderScanner orderScanner = new OrderFileScanner(orderFileName);
        orderScanner.setIngestionRate(ingestionRate);
        return orderScanner;
    }

    @Autowired
    public IOrderScanner orderScanner;

    @Autowired
    public IOrderObserver kitchenOrderReceiver;

    @Autowired
    public ICourierService courierService;

    @Autowired
    public IKitchenService kitchenService;

    public static void main(final String[] args) throws IOException {
        SpringApplication.run(KitchenApp.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            orderScanner.setIngestionRate(ingestionRate);  // set ingestion rate as 10 millisecond to seed up the test.
            orderScanner.registerOrderObserver(kitchenOrderReceiver);
            orderScanner.startOrderScanner();

            kitchenService.openKitchen(kitchenOrderReceiver);
            courierService.start();
        };
    }
}

package com.ech.kitchen;

import com.ech.kitchen.courier.service.ICourierService;
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
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.Arrays;

@SpringBootApplication
@PropertySource("classpath:/application.properties")
@Slf4j
public class KitchenApp {
    @Autowired
    private AbstractEnvironment env;

    @Value("${order.file.name:order.json}")
    private String orderFileName;

    @Value("${order.ingestion.rate:100}")
    private long ingestionRate;

    @Bean
    IOrderScanner orderScanner() {
        return new OrderFileScanner(orderFileName, ingestionRate);
    }

    @Autowired
    private IOrderScanner orderScanner;

    @Autowired
    private IOrderObserver kitchenOrderReceiver;

    @Autowired
    private ICourierService courierService;

    @Autowired
    private IKitchenService kitchenService;

    public static void main(final String[] args) throws IOException {
        SpringApplication.run(KitchenApp.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        showAppSettings();
        return args -> {
            orderScanner.setIngestionRate(ingestionRate);
            orderScanner.registerOrderObserver(kitchenOrderReceiver);
            orderScanner.startOrderScanner();

            kitchenService.openKitchen(kitchenOrderReceiver);
            courierService.start();
        };
    }

    private void showAppSettings() {
        log.info("---------- CSS Application Settings Begin----------");
        env.getPropertySources()
                .stream()
                .filter(ps -> ps instanceof ResourcePropertySource)
                .map(ps -> (ResourcePropertySource) ps)
                .flatMap(rps -> Arrays.asList(rps.getPropertyNames()).stream())
                .forEach(name -> log.info("{}={}", name, env.getProperty(name)));
        log.info("---------- CSS Application Settings End----------");
    }

}

package com.ech.order.impl;

import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ofMillis;

@Component
@Slf4j
public class OrderFileScanner implements IOrderScanner {
    @Value("${order.file.name:orders.json}")
    private String orderFileName;

    public OrderFileScanner() {}

    public OrderFileScanner(String orderFileName) {
        this.orderFileName = orderFileName;
    }

    public List<Order> readAllOrders() {
        ObjectMapper objectMapper = new ObjectMapper();

        List<Order> orders = new ArrayList<>();
        File jsonFile = Paths.get(getOrderFilePath()).toFile();
        try {
            orders = objectMapper.readValue(jsonFile, new TypeReference<List<Order>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            log.error(String.format("Failed to parse the json from file %s", orderFileName));
        }
        log.info("There are {} orders are read from file.", orders.size());
        return orders;
    }

    public void registerOrderReceiver(IOrderObserver orderReceiver) {
        Flux.interval(ofMillis(10))
                .zipWithIterable(readAllOrders())
                .map(t -> t.getT2())
                .subscribe(orderReceiver);
        log.info("A subscriber registered on order receiver.");
    }

    private String getOrderFilePath() {
        try {
            final URL jsonFileURL = this.getClass().getModule().getClassLoader().getResource(orderFileName);
            final String jsonFile = Paths.get(jsonFileURL.toURI()).toFile().getAbsolutePath();
            log.info("Find the orders file in {}", jsonFile);

            return jsonFile;
        } catch (Exception e) {
            log.error("Cannot find the order file {}.", orderFileName);
            return orderFileName;
        }
    }

}

package com.ech.order.impl;

import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.mo.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.time.Duration.ofMillis;

@Component
@PropertySource("classpath:/application.properties")
@Slf4j
public class OrderFileScanner implements IOrderScanner {

    @Getter
    @Setter
    @Value("${order.file.name:order.json}")
    private String orderFile;

    @Getter
    @Setter
    @Value("${order.ingestion.rate:2000}")
    private long ingestionRate;

    private Set<IOrderObserver> orderObserverSet = new HashSet<>();

    public OrderFileScanner() {
    }

    public OrderFileScanner(String orderFile) {
        this.orderFile = orderFile;
    }

    public OrderFileScanner(String orderFile, long ingestionRate) {
        this.orderFile = orderFile;
        this.ingestionRate = ingestionRate;
    }

    /**
     * Read the order json file and parsed its content as list of Order objects.
     *
     * @return
     */
    public List<Order> readAllOrders() {
        List<Order> orders = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File orderFile = findOrderFile();
            orders = objectMapper.readValue(orderFile, new TypeReference<List<Order>>() {
            });
        } catch (Exception e) {
            log.error("Failed to parse the order json from file.", e);
        }
        log.info("There are {} orders are read from file.", orders.size());
        return orders;
    }

    @Override
    public Flux<Order> readOrderAsFlux() {
        return Flux.interval(ofMillis(ingestionRate))
                .zipWithIterable(readAllOrders())
                .map(t -> t.getT2());
    }

    public void registerOrderObserver(IOrderObserver orderObserver) {
        orderObserverSet.add(orderObserver);
        log.info("A subscriber registered on order receiver.");
    }

    @Override
    public Set<IOrderObserver> getAllOrderObserver() {
        return orderObserverSet;
    }

    @Override
    public void unRegisterOrderObserver(IOrderObserver orderObserver) {
        orderObserverSet.remove(orderObserver);
    }

    @Override
    public Flux<Order> startOrderScanner() {
        log.info("Begin to scan the order file {}.", orderFile);
        final Flux<Order> orderFlux = readOrderAsFlux();
        for (IOrderObserver observer : orderObserverSet) {
            orderFlux.subscribe(observer);
        }
        return orderFlux;
    }

    private File findOrderFile() {
        log.info("orderFile={}", orderFile);
        String jsonFilePath = orderFile.trim();
        try {
            // If order file path is relative path, try to figure out its abstract path.
            if (!orderFile.startsWith(File.separator)) {
                URL jsonFileURL = this.getClass().getModule().getClassLoader().getResource(orderFile);
                if (jsonFileURL == null) {
                    jsonFileURL = this.getClass().getClassLoader().getResource(orderFile);
                }
                jsonFilePath = Paths.get(jsonFileURL.toURI()).toFile().getAbsolutePath();
            }
        } catch (Exception e) {
            log.info("Failed to figure out its abstract path. Use it as abstract path");
        }

        final File jsonFile = Paths.get(jsonFilePath).toFile();
        log.info("Find the orders file in {}", jsonFilePath);

        return jsonFile;
    }

}

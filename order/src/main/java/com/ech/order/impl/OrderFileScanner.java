package com.ech.order.impl;

import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.mo.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderFileScanner implements IOrderScanner {

    private String orderFile;

    private Set<IOrderObserver> orderObserverSet = new HashSet<>();

    public OrderFileScanner() {}

    public OrderFileScanner(String orderFile) {
        this.orderFile = orderFile;
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
            orders = objectMapper.readValue(orderFile, new TypeReference<List<Order>>(){});
        } catch (Exception e) {
            log.error("Failed to parse the order json from file.", e);
        }
        log.info("There are {} orders are read from file.", orders.size());
        return orders;
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
    public void startOrderScanner() {
        log.info("Begin to scan the order file.");
        final Flux<Order> orderFlux = Flux.interval(ofMillis(10))
                .zipWithIterable(readAllOrders())
                .map(t -> t.getT2());
        for (IOrderObserver observer : orderObserverSet) {
            orderFlux.subscribe(observer);
        }
    }

    public String getOrderFile() {
        return orderFile;
    }

    public void setOrderFile(String orderFile) {
        this.orderFile = orderFile;
    }

    private File findOrderFile() throws Exception {
        String jsonFilePath = orderFile;
        if (!orderFile.startsWith(File.separator)) {
            final URL jsonFileURL = this.getClass().getModule().getClassLoader().getResource(orderFile);
            jsonFilePath = Paths.get(jsonFileURL.toURI()).toFile().getAbsolutePath();
        }
        final File jsonFile = Paths.get(jsonFilePath).toFile();
        log.info("Find the orders file in {}", jsonFilePath);

        return jsonFile;
    }

}

package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.Kitchen;
import com.ech.kitchen.service.ICookedOrderPickStrategy;
import com.ech.kitchen.service.ICourierService;
import com.ech.kitchen.service.IExpiredOrderCheckingService;
import com.ech.kitchen.service.IKitchenService;
import com.ech.kitchen.service.IShelfSelectStrategy;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.impl.OrderFileScanner;
import com.ech.order.mo.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CssApplicationTestConfig {

    @Value("${order.file.name:orders_5.json}")
    private String orderFileName;

    @Value("${order.ingestion.rate:500}")
    int ingestionRate;

    @Value("#{${kitchen.shelf.capacity}}")
    private Map<String, Integer> shelfCapacities;

    @Value("${kitchen.shelf.capacity.default}")
    private int shelfDefaultCapacity;

    @Value("${courier.worker.thread.pool.size:20}")
    private int courierWorkerThreadPoolSize;

    @Value("${courier.sleep.min:2000}")
    private int minInterval;

    @Value("${courier.sleep.min:6000}")
    private int maxInterval;

    @Value("${kitchen.order.expire.checker.delay:2000}")
    private long checkerInitialDelay;

    @Value("${kitchen.order.expire.checker.period:5000}")
    private long checkerPeriod;

    @Bean
    public IOrderScanner orderScanner() {
        return new OrderFileScanner(orderFileName, ingestionRate);
    }

    @Bean
    public IOrderObserver<Order> orderObserver() {
        return new KitchenOrderObserver();
    }

    @Bean
    public IShelfSelectStrategy shelfChoicer() {
        return new StrategyPutOrderOnShelf();
    }

    @Bean
    public IExpiredOrderCheckingService pickupAreaRecycleService() {
        return new ExpiredOrderChecker();
    }

    @Bean
    public ICookedOrderPickStrategy cookedOrderPickStrategy() {
        return new CookedOrderRandomPicker();
    }

    @Bean
    public IKitchenService kitchenService() {
        return new KitchenService();
    }

    @Bean
    public ICourierService courierService() {
        final ICourierService courierService = new CourierService();
        ((CourierService) courierService).setMaxInterval(minInterval);
        ((CourierService) courierService).setMaxInterval(maxInterval);
        ((CourierService) courierService).setMaxInterval(courierWorkerThreadPoolSize);
        return courierService;
    }

    @Bean
    public IExpiredOrderCheckingService expiredOrderCheckingService() {
        final ExpiredOrderChecker expiredOrderChecker = new ExpiredOrderChecker();
        expiredOrderChecker.setCheckerInitialDelay(checkerInitialDelay);
        expiredOrderChecker.setCheckerPeriod(checkerPeriod);
        return expiredOrderChecker;
    }

    @Bean
    public Kitchen kitchen() {
        final Kitchen kitchen = new Kitchen();
        kitchen.setShelfCapacities(shelfCapacities);
        kitchen.setShelfDefaultCapacity(shelfDefaultCapacity);
        kitchen.buildPickupArea();
        return kitchen;
    }
}

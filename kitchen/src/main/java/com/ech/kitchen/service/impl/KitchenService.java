package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Kitchen;
import com.ech.kitchen.service.IKitchenService;
import com.ech.kitchen.service.IOrderOnShelfTTLCalculateStrategy;
import com.ech.kitchen.service.IShelfSelectStrategy;
import com.ech.order.IOrderObserver;
import com.ech.order.mo.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class KitchenService implements IKitchenService {

    @Autowired
    private IOrderObserver<Order> orderObserver;

    @Autowired
    private IShelfSelectStrategy shelfChoicer;

    @Autowired
    private IOrderOnShelfTTLCalculateStrategy orderValueCalculator;

    private AtomicLong counter = new AtomicLong();

    private Executor singleExecutor = Executors.newSingleThreadExecutor();

    public IOrderObserver<Order> getOrderObserver() {
        return orderObserver;
    }

    private volatile boolean isOpen = false;

    @Autowired
    private Kitchen kitchen;

    @Override
    public void openKitchen(IOrderObserver<Order> orderObserver) {
        if (isOpen) {
            log.warn("The kitchen is opened already.");
        }

        isOpen = true;
        this.orderObserver = orderObserver;
        this.orderObserver.beginObserve();
        processIncomingOrders();
    }

    private void processIncomingOrders() {
        singleExecutor.execute(() -> {
            while (isOpen || orderObserver.hasNext()) {
                final Optional<Order> orderOptional = orderObserver.nextOrder();
                if (orderOptional.isEmpty()) {
                    log.warn("There's no order for a while. Let's wait ...");
                } else {
                    final CookedOrder cookedOrder = new CookedOrder();
                    final Order order = orderOptional.get();
                    cookedOrder.setOrder(order);

                    log.info("An order in kitchen ... {}", counter.incrementAndGet());
                    cookedOrder.setReceivedByKitchenTime(Instant.now());

                    log.info("Cook Done! {}", order);
                    cookedOrder.setCookedDoneTime(Instant.now());

                    shelfChoicer.putOrderOnShelf(kitchen.getPickupArea(), cookedOrder);
                }
            }
            log.info("Kitchen closed. isOpen={}", isOpen);
        });
    }

    @Override
    public void setShelfChoiceStrategy(IShelfSelectStrategy strategy) {
        shelfChoicer = strategy;
    }

    @Override
    public void setOrderValueCalculateStrategy(IOrderOnShelfTTLCalculateStrategy strategy) {
        orderValueCalculator = strategy;
    }

    @Override
    public long processedOrderAmount() {
        return counter.longValue();
    }

    @Override
    public void closeKitchen() {
        if (!isOpen) {
            log.warn("The kitchen is closed already.");
        }
        isOpen = false;
        log.info("Set isOpen={} and stop receive orders.", isOpen);
        orderObserver.stopObserve();
    }

}

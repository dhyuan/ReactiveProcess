package com.ech.kitchen.service.impl;

import com.ech.kitchen.service.IKitchenService;
import com.ech.kitchen.entity.Kitchen;
import com.ech.order.IOrderObserver;
import com.ech.order.mo.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class KitchenService implements IKitchenService {

    @Autowired
    public IOrderObserver<Order> orderObserver;

    private AtomicLong counter = new AtomicLong();

    private Executor singleExecutor = Executors.newSingleThreadExecutor();

    public IOrderObserver<Order> getOrderObserver() {
        return orderObserver;
    }

    private volatile boolean isOpen = false;

    @Autowired
    private Kitchen kitchen;

    @Override
    public void openKitchen(IOrderObserver<Order> orderReceiver) {
        if (isOpen) {
            log.warn("The kitchen is opened already.");
        }
        orderObserver = orderReceiver;
        orderObserver.beginObserve();
        cookForInComingOrders();
        isOpen = true;
    }

    private void cookForInComingOrders() {
        singleExecutor.execute(() -> {
            while (isOpen || orderObserver.hasNext()) {
                final Optional<Order> orderOptional = orderObserver.nextOrder();
                if (orderOptional.isEmpty()) {
                    log.warn("There's no order for a while. Let's waiting ...");
                } else {
                    final Order order = orderOptional.get();
                    log.info("An order in kitchen ... {}", counter.incrementAndGet());
                    log.info("Cook Done! {}", order);
                }
            }
            log.info("Kitchen closed.");
        });
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
        orderObserver.stopObserve();
    }


}

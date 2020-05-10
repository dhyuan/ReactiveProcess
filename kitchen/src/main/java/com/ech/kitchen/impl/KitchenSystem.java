package com.ech.kitchen.impl;

import com.ech.kitchen.IKitchenSystem;
import com.ech.order.IOrderObserver;
import com.ech.order.Order;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class KitchenSystem implements IKitchenSystem {
    private static final Logger LOG = LogManager.getLogger(KitchenSystem.class);

    @Autowired
    public IOrderObserver<Order> orderObserver;

    private AtomicLong counter = new AtomicLong();

    private Executor singleExecutor = Executors.newSingleThreadExecutor();

    public IOrderObserver<Order> getOrderObserver() {
        return orderObserver;
    }

    private volatile boolean isOpen = false;

    @Override
    public void openKitchen(IOrderObserver<Order> orderReceiver) {
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
                    LOG.warn("There's no order for a while. Let's waiting ...");
                } else {
                    final Order order = orderOptional.get();
                    LOG.info("An order in kitchen ... {}", counter.incrementAndGet());
                    LOG.info("Cook Done! {}", order);
                }
            }
            LOG.info("Kitchen closed.");
        });
    }

    @Override
    public long processedOrderAmount() {
        return counter.longValue();
    }

    @Override
    public void closeKitchen() {
        orderObserver.stopObserve();
        isOpen = false;
    }


}

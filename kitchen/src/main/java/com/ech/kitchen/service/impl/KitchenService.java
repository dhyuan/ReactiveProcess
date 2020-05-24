package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Kitchen;
import com.ech.kitchen.service.ICookedOrderPickStrategy;
import com.ech.kitchen.service.ICookedOrderProvider;
import com.ech.kitchen.service.IExpiredOrderCheckingService;
import com.ech.kitchen.service.IKitchenService;
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
public class KitchenService implements IKitchenService, ICookedOrderProvider {

    @Autowired
    private IOrderObserver<Order> orderObserver;

    @Autowired
    private IShelfSelectStrategy shelfChoicer;

    @Autowired
    private ICookedOrderPickStrategy cookedOrderPickStrategy;

    @Autowired
    private IExpiredOrderCheckingService pickupAreaRecycleService;

    private AtomicLong counter = new AtomicLong();

    private Executor singleExecutor = Executors.newSingleThreadExecutor();

    private Executor couriers = Executors.newFixedThreadPool(3);

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

        pickupAreaRecycleService.workOn(kitchen.getShelvesInPickupArea());
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
    public void setPickupAreaCleanService(IExpiredOrderCheckingService pickupAreaRecycleService) {
        this.pickupAreaRecycleService = pickupAreaRecycleService;
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

    @Override
    public Optional<CookedOrder> provideCookedOrder() {
        final Optional<CookedOrder> cookedOrder = cookedOrderPickStrategy.pickupFrom(kitchen.getShelvesInPickupArea());
        if (cookedOrder.isPresent()) {
            log.info("Pull out an order from shelf. orderId:{}", cookedOrder.get().getOrder().getId());
        } else {
            log.info("There is no order on kitchen shelves.");
        }
        return cookedOrder;
    }
}

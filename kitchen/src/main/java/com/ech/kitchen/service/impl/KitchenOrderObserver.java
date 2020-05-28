package com.ech.kitchen.service.impl;

import com.ech.order.IOrderObserver;
import com.ech.order.mo.Order;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class KitchenOrderObserver implements IOrderObserver<Order> {

    @Value("${kitchen.order.poll.amount.onetime:1}")
    private static long ORDER_PULL_TIMEOUT = 1;

    @Value("${kitchen.order.amount.per.req:5}")
    private static long ORDER_AMOUNT_IN_ONE_REQ = 5;

    private Subscription orderSubscription;

    private final BlockingDeque<Order> internalOrderQueue = new LinkedBlockingDeque<>();

    private volatile boolean isObserving = false;

    @Override
    public void beginObserve() {
        if (!isObserving) {
            log.info("Kitchen begin to receive orders ...");
            orderSubscription.request(ORDER_AMOUNT_IN_ONE_REQ);
            isObserving = true;
        }
    }

    @Override
    public void stopObserve() {
        if (isObserving) {
            log.info("Kitchen closing ... not to receive orders ...");
            orderSubscription.cancel();
        }
    }

    @Override
    public boolean isObserving() {
        return isObserving;
    }

    @Override
    public boolean hasNext() {
        return !internalOrderQueue.isEmpty();
    }

    @Override
    public Optional<Order> nextOrder() {
        try {
            final Order order = internalOrderQueue.pollFirst(ORDER_PULL_TIMEOUT, TimeUnit.MINUTES);
            if (order != null) {
                log.info("An order received. {}", order);
                return Optional.of(order);
            }
        } catch (InterruptedException e) {
            log.warn("Exception occurred while waiting next order: {}", e.getMessage());
        }

        log.warn("There is no order available in {} minutes.", ORDER_PULL_TIMEOUT);
        return Optional.empty();
    }

    /** The following are implementation for org.reactivestreams.Subscriber. */

    @Override
    public void onSubscribe(Subscription subscription) {
        log.info("Kitchen are ready to receive orders.");
        this.orderSubscription = subscription;
    }

    @Override
    public void onNext(Order order) {
        log.debug("A new order coming. {}", order);
        final boolean isPutInQueue = internalOrderQueue.offerLast(order);
        log.info("A new order was accepted by kitchen.");
        orderSubscription.request(ORDER_AMOUNT_IN_ONE_REQ);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error occurred while receiving order: {}", throwable.getMessage());
    }

    @Override
    public void onComplete() {
        log.info("All order are received in kitchen.");
    }
}

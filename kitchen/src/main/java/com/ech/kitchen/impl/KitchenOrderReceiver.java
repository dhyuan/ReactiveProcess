package com.ech.kitchen.impl;

import com.ech.order.IOrderObserver;
import com.ech.order.Order;
//import com.ech.css.order.impl.OrderFileScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Component
public class KitchenOrderReceiver implements IOrderObserver<Order> {
    private static final Logger LOG = LogManager.getLogger(KitchenOrderReceiver.class);

    @Value("${kitchen.order.poll.amount.onetime:1}")
    private static long ORDER_PULL_TIMEOUT = 1;

    @Value("${kitchen.order.amount.per.req:5}")
    private static long ORDER_AMOUNT_IN_ONE_REQ = 5;

    private Subscription orderSubscription;

    private final BlockingDeque<Order> internalOrderQueue = new LinkedBlockingDeque<>();

    private volatile boolean isObserving = false;

    public void beginObserve() {
        if (!isObserving) {
            LOG.info("Kitchen begin to receive orders as much as possible.");
            orderSubscription.request(ORDER_AMOUNT_IN_ONE_REQ);
            isObserving = true;
        } else {
            LOG.info("Kitchen already opened.");
        }
    }

    @Override
    public void stopObserve() {
        if (isObserving) {
            LOG.info("Kitchen closing ... not to receive orders ...");
            orderSubscription.cancel();
        } else {
            LOG.info("Kitchen already closed.");
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
                LOG.info("An order received. {}", order);
                return Optional.of(order);
            }
        } catch (InterruptedException e) {
            LOG.warn("Exception occurred while waiting next order: {}", e.getMessage());
        }

        LOG.warn("There is no order available in {} minutes.", ORDER_PULL_TIMEOUT);
        return Optional.empty();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        LOG.info("Kitchen are ready to receive orders.");
        this.orderSubscription = subscription;
    }

    @Override
    public void onNext(Order order) {
        LOG.debug("A new order coming. {}", order);
        internalOrderQueue.offerLast(order);
        orderSubscription.request(ORDER_AMOUNT_IN_ONE_REQ);
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Error occurred while receiving order: {}", throwable.getMessage());
    }

    @Override
    public void onComplete() {
        LOG.info("All order are received.");
        orderSubscription.cancel();
    }
}

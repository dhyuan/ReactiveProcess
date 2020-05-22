package com.ech.order.impl;

import com.ech.order.IOrderObserver;
import com.ech.order.mo.Order;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OrderObserverAdapter<Order> implements IOrderObserver<Order> {

    public void beginObserve() {
        log.info("beginObserve.");
    }

    @Override
    public void stopObserve() {
        log.info("stopObserve");
    }

    @Override
    public boolean isObserving() {
        return false;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Optional<Order> nextOrder() {
        return Optional.empty();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
    }

    @Override
    public void onNext(Order order) {
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error occurred while receiving order: {}", throwable.getMessage());
    }

    @Override
    public void onComplete() {
    }
}

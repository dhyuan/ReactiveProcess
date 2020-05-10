package com.ech.order;

import org.reactivestreams.Subscriber;

import java.util.Optional;

public interface IOrderObserver<Order> extends Subscriber<Order> {

    void beginObserve();
    void stopObserve();
    boolean isObserving();

    boolean hasNext();
    Optional<Order> nextOrder();
}

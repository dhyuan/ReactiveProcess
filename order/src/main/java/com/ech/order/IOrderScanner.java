package com.ech.order;

import com.ech.order.mo.Order;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

/**
 * This interface defines the method need to implement to :
 * 1) read the orders
 * 2) register the order listener as observer.
 */
public interface IOrderScanner {

    List<Order> readAllOrders();

    Flux<Order> readOrderAsFlux();

    void registerOrderObserver(IOrderObserver orderObserver);

    void unRegisterOrderObserver(IOrderObserver orderObserver);

    Set<IOrderObserver> getAllOrderObserver();

    long getIngestionRate();

    void setOrderFile(String orderFile);

    void setIngestionRate(long timePerOneOrder);

    Flux<Order> startOrderScanner();
}

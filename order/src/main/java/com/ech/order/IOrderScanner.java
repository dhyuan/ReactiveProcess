package com.ech.order;

import com.ech.order.mo.Order;

import java.util.List;

/**
 * This interface defines the method need to implement to :
 * 1) read the orders
 * 2) register the order listener as observer.
 */
public interface IOrderScanner {

    /**
     * The order can come form different source such as DB, network or files.
     * To simplify the demo, let's just use the sync type List other than streaming type Flux.
     *
     * @return The list of order.
     */
    List<Order> readAllOrders();

    void registerOrderReceiver(IOrderObserver orderObserver);
}

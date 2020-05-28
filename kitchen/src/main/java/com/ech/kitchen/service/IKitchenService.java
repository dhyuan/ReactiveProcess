package com.ech.kitchen.service;

import com.ech.order.IOrderObserver;
import com.ech.order.mo.Order;

/**
 * This interface defines what the kitchen system need to do:
 * 1) Open kitchen:
 * Receives the orders by employing an instance of IOrderObserver<Order>.
 * And 'works' start to cook for the incoming orders and put them on a shelf.
 * 2) Closes the kitchen to stop receiving orders.
 * 3) Records the number of order processed.
 */
public interface IKitchenService {

    /**
     * Set an IShelfSelectStrategy implementation for kitchen system to let it know
     * how to pick a cooked order to deliver.
     *
     * @param strategy
     */
    void setShelfChoiceStrategy(IShelfSelectStrategy strategy);

    /**
     * Set an IExpiredOrderCheckingService implementation for kitchen system to let it know
     * where/how to put a cooked order on a shelf.
     *
     * @param pickupAreaCleanService
     */
    void setPickupAreaCleanService(IExpiredOrderCheckingService pickupAreaCleanService);

    /**
     * After this method is called, the kitchen begin to receive orders.
     *
     * @param orderObserver This parameter implements the IOrderObserver which observes on IOrderScanner.
     */
    void openKitchen(IOrderObserver<Order> orderObserver);

    /**
     * Statistic about how many orders are received by the kitchen system.
     *
     * @return the number of orders received by the kitchen system.
     */
    long totalIncomingOrderNumb();

    /**
     * When this method is called, the kitchen will
     * 1) Stop to receive new orders
     * 2) Close the kitchen system after the existed/received orders are processed.
     */
    void closeKitchen();

}

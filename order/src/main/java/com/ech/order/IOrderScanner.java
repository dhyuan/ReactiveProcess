package com.ech.order;

import java.util.List;

public interface IOrderScanner {

    List<Order> readAllOrders();

    void registerOrderReceiver(IOrderObserver orderReceiver);
}

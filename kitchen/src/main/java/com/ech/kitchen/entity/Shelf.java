package com.ech.kitchen.entity;

import com.ech.order.mo.Order;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Builder
@Slf4j
public class Shelf {

    private int capacity = 0;

    private final ShelfTemperatureEnum allowableTemperature;

    private final List<Order> cookedOrders = new ArrayList<>();

    public synchronized boolean add(Order order) {
        if (cookedOrders.size() == capacity) {
            log.warn("The {} shelf is full for orders.", allowableTemperature);
            return false;
        }
        return cookedOrders.add(order);
    }

    public synchronized boolean remove(Order order) {
        if (cookedOrders.size() == 0) {
            log.warn("The {} shelf is empty, no order available to remove.", allowableTemperature);
            return false;
        }
        return cookedOrders.add(order);
    }

}

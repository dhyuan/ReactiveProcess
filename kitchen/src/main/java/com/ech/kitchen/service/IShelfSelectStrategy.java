package com.ech.kitchen.service;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.mo.ShelfTemperatureEnum;

import java.util.Map;

public interface IShelfSelectStrategy {

    /**
     * Place the cookedOrder in a shelf. Subclass can provided different algorithm to decide how to choose a shelf.
     *
     * @param pickupArea
     * @param cookedOrder
     */
    void putOrderOnShelf(Map<ShelfTemperatureEnum, Shelf> pickupArea, CookedOrder cookedOrder);

    /**
     * @return The total number of dropped orders from shelves.
     */
    long droppedOrderNumb();
}

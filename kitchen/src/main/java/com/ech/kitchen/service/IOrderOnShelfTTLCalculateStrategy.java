package com.ech.kitchen.service;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;

/**
 * Implements this interface to provide the way to calculate how long an order can exist on a shelf.
 */
public interface IOrderOnShelfTTLCalculateStrategy {

    float calculateOrderOnShelfTTL(CookedOrder cookedOrder, Shelf shelf);
}

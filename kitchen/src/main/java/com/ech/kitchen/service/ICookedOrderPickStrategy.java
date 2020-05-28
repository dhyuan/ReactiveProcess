package com.ech.kitchen.service;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;

import java.util.List;
import java.util.Optional;

/**
 * Subclass to implement this interface to provide an algorithm about how to
 * pick up a cooked order from shelves to deliver.
 */
public interface ICookedOrderPickStrategy {

    /**
     * Choose a CookedOrder from the shelves.
     *
     * @param shelves where the cooked order choose from.
     * @return the cooked order to deliver.
     */
    Optional<CookedOrder> pickupFrom(List<Shelf> shelves);

}

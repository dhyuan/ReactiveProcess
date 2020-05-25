package com.ech.kitchen.service;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;

import java.util.List;
import java.util.Optional;

/**
 * Choose an cooked order from shelves.
 */
public interface ICookedOrderPickStrategy {

    Optional<CookedOrder> pickupFrom(List<Shelf> shelves);

}

package com.ech.kitchen.courier.service;

import com.ech.kitchen.mo.CookedOrder;

import java.util.Optional;

/**
 * Works as bridge of kitchen system and courier system.
 */
public interface ICookedOrderProvider {

    /**
     * Implements this method to provide cooked order.
     *
     * @return the cooked order to delivery.
     */
    Optional<CookedOrder> provideCookedOrder();

}

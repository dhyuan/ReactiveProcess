package com.ech.kitchen.courier.service;

import com.ech.kitchen.mo.CookedOrder;

import java.util.Optional;

/**
 * Defines methods to control the Courier system to start, stop and cooked order source.
 */
public interface ICourierService {

    /**
     * start the courier service.
     */
    void start();

    /**
     * stop the courier service.
     */
    void stop();

    /**
     * Use ICookedOrderProvider as the source of order to deliver.
     *
     * @param provider
     * @return
     */
    Optional<CookedOrder> requestCookedOrder(ICookedOrderProvider provider);

    /**
     * Statistic of how many cooked orders are delivered.
     *
     * @return
     */
    long deliveryCount();

    /**
     * Statistic of how many cooked orders are failed to delivered.
     *
     * @return
     */
    long deliveryErrorCount();


}

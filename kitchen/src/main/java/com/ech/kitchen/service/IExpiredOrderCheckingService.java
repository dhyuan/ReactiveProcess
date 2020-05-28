package com.ech.kitchen.service;

import com.ech.kitchen.mo.Shelf;

import java.util.Collection;

/**
 * Subclass implements this interface to provide the function of
 * clear the expired cooked order on shelves.
 */
public interface IExpiredOrderCheckingService {

    /**
     * check the cooked order on shelves and remove the expired orders.
     *
     * @param shelves the shelves to be checked.
     */
    void check(Collection<Shelf> shelves);

    /**
     * @return total number of expired order after the system up.
     */
    long totalExpiredOrderNumb();

    /**
     * @return total number of failure to remove expired order. Expect to be 0 always.
     */
    long totalFailedCleanNumb();

}

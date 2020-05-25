package com.ech.kitchen.service;

import com.ech.kitchen.mo.Shelf;

import java.util.Collection;

public interface IExpiredOrderCheckingService {

    void workOn(Collection<Shelf> shelves);

    long totalExpiredOrderNumb();
    long totalFailedCleanNumb();
}

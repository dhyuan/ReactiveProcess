package com.ech.kitchen.service;

import com.ech.kitchen.mo.Shelf;

import java.util.Collection;

public interface IPickupAreaRecycleService {

    void workOn(Collection<Shelf> shelves);
}

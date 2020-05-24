package com.ech.kitchen.service;

import com.ech.kitchen.mo.CookedOrder;

import java.util.Optional;

public interface ICourierService {

    void start();

    Optional<CookedOrder> requestCookedOrder(ICookedOrderProvider provider);

    void stop();
}

package com.ech.kitchen.service;

import com.ech.kitchen.mo.CookedOrder;

import java.util.Optional;

public interface ICookedOrderProvider {

    Optional<CookedOrder> provideCookedOrder();

}

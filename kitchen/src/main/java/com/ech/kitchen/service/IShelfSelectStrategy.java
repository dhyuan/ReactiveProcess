package com.ech.kitchen.service;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.mo.ShelfTemperatureEnum;

import java.util.Map;

public interface IShelfSelectStrategy {

    void putOrderOnShelf(Map<ShelfTemperatureEnum, Shelf> pickupArea, CookedOrder cookedOrder);

}

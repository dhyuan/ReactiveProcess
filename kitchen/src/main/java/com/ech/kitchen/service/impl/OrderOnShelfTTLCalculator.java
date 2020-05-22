package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.service.IOrderOnShelfTTLCalculateStrategy;
import org.springframework.stereotype.Component;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;

@Component()
public class OrderOnShelfTTLCalculator implements IOrderOnShelfTTLCalculateStrategy {

    @Override
    public float calculateOrderOnShelfTTL(CookedOrder cookedOrder, Shelf shelf) {
        final Integer shelfLife = cookedOrder.getOrder().getShelfLife();
        final Float decayRate = cookedOrder.getOrder().getDecayRate();
        final long orderAge = cookedOrder.getOrderAge();

        return (shelfLife - decayRate * orderAge * getShelfDecayModifier(shelf)) / shelfLife;
    }

    private int getShelfDecayModifier(Shelf shelf) {
        return shelf.getAllowableTemperature() == Any ? 2 : 1;
    }

}

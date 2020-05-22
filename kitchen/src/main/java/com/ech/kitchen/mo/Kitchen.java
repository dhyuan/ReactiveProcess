package com.ech.kitchen.mo;


import com.ech.order.IOrderObserver;
import com.ech.order.mo.Order;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class Kitchen {

    @Getter
    @Setter
    private Map<String, Integer> shelfCapacities;

    @Getter
    @Setter
    private int shelfDefaultCapacity;

    private final Map<ShelfTemperatureEnum, Shelf> pickupArea = new HashMap<>();

    @Autowired
    private IOrderObserver orderObserver;

    public Kitchen(){
    }

    public void buildPickupArea() {
        for (ShelfTemperatureEnum shelfTemp : ShelfTemperatureEnum.values()) {
            final Shelf shelf = Shelf.builder().allowableTemperature(shelfTemp).build();
            final Integer capacity = shelfCapacities.getOrDefault(shelfTemp.name(), shelfDefaultCapacity);
            shelf.setMaxCapacity(capacity);
            pickupArea.put(shelfTemp, shelf);
            log.info("Add {} in kitchen.", shelf);
        }
    }

    public Map<ShelfTemperatureEnum, Shelf> getPickupArea() {
        return this.pickupArea;
    }

}



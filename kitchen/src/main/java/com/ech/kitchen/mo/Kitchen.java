package com.ech.kitchen.mo;


import com.ech.order.IOrderObserver;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class Kitchen {

    @Getter @Setter
    @Value("#{${kitchen.shelf.capacity}}")
    private Map<String, Integer> shelfCapacities;

    @Getter @Setter
    @Value("${kitchen.shelf.capacity.default}")
    private int shelfDefaultCapacity;

    private final Map<ShelfTemperatureEnum, Shelf> pickupArea = new ConcurrentHashMap<>();

    @Autowired
    private IOrderObserver orderObserver;

    public Kitchen(){
    }

    @PostConstruct
    public void buildPickupArea() {
        for (ShelfTemperatureEnum shelfTemp : ShelfTemperatureEnum.values()) {
            final Integer capacity = shelfCapacities.getOrDefault(shelfTemp.name(), shelfDefaultCapacity);
            final Shelf shelf = new Shelf(shelfTemp, capacity);
            pickupArea.put(shelfTemp, shelf);
            log.info("Add {} in kitchen.", shelf);
        }
    }

    public Map<ShelfTemperatureEnum, Integer> getNumbersOfOrderOnShelf() {
        Map<ShelfTemperatureEnum, Integer> counters = new HashMap<>();
        for (ShelfTemperatureEnum shelfTemp : ShelfTemperatureEnum.values()) {
            counters.put(shelfTemp, pickupArea.get(shelfTemp).currentOrderNumb());
        }
        return counters;
    }

    public long getTotalNumberOfOrderOnShelf() {
        return getNumbersOfOrderOnShelf().values().stream().reduce(0, (i1, i2) -> i1 + i2);
    }

    public Map<ShelfTemperatureEnum, Shelf> getPickupArea() {
        return this.pickupArea;
    }

    public List<Shelf> getShelvesInPickupArea() {
        return new ArrayList<>(pickupArea.values());
    }
}



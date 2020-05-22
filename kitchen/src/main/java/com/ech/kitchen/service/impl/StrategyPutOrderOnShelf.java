package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.mo.ShelfFullException;
import com.ech.kitchen.mo.ShelfTemperatureEnum;
import com.ech.kitchen.service.IShelfSelectStrategy;
import com.ech.order.mo.Order;
import com.ech.order.mo.OrderTemperatureEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Cold;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Frozen;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Hot;

@Component
@Slf4j
public class StrategyPutOrderOnShelf implements IShelfSelectStrategy {

    @Override
    public void putOrderOnShelf(Map<ShelfTemperatureEnum, Shelf> pickupArea, CookedOrder cookedOrder) {
        final ShelfTemperatureEnum matchedShelfTemp = mapToShelfTemperature(cookedOrder.getOrder().getTemp());
        final Shelf shelf = pickupArea.get(matchedShelfTemp);
        try {
            shelf.add(cookedOrder);
        } catch (ShelfFullException e) {
            log.warn("{} is full. Try to put on the overflow shelf.", shelf);
            putOrderOnOverflowShelf(cookedOrder, pickupArea);
        }
    }

    public void putOrderOnOverflowShelf(CookedOrder cookedOrder, Map<ShelfTemperatureEnum, Shelf> pickupArea) {
        Shelf overflowShelf = pickupArea.get(Any);
        synchronized (overflowShelf) {
            try {
                overflowShelf.add(cookedOrder);
            } catch (ShelfFullException e) {
                log.warn("The overflow shelf is full.");
                cleanupsOverflowShelfAndTryAgain(cookedOrder, pickupArea, overflowShelf);
            }
        }
    }

    private void cleanupsOverflowShelfAndTryAgain(CookedOrder cookedOrder,
                                                  Map<ShelfTemperatureEnum, Shelf> pickupArea,
                                                  Shelf overflowShelf) {
        reassignOrdersOnOverFlowShelf(overflowShelf, pickupArea);

        if (overflowShelf.availableSpace() < 1) {
            log.info("No cookedOrder is moved to other shelf from overflow shelf. Drop an cookedOrder on overflow shelf randomly");
            final int rndIndex = new Random().nextInt(overflowShelf.getMaxCapacity());
            overflowShelf.remove(rndIndex);
        }

        final boolean isAdded = overflowShelf.addWithoutException(cookedOrder);
        if (isAdded) {
            log.info("{} is added on overflow shelf after clear up.", cookedOrder);
        } else {
            log.error("Cannot add {} on shelf even after clear up the overflow shelf !!!", cookedOrder);
        }
    }

    private void reassignOrdersOnOverFlowShelf(Shelf overflowShelf, Map<ShelfTemperatureEnum, Shelf> pickupArea) {
        log.info("Try to reassign the orders on overflow shelf to other shelves");
        final Map<OrderTemperatureEnum, List<CookedOrder>> ordersMap =
                overflowShelf.getCookedOrders().stream().collect(Collectors.groupingBy(o -> o.getOrder().getTemp()));

        for (OrderTemperatureEnum orderTemp : ordersMap.keySet()) {
            final Shelf shelf = pickupArea.get(mapToShelfTemperature(orderTemp));
            final List<CookedOrder> orders = ordersMap.get(orderTemp);
            if (CollectionUtils.isNotEmpty(orders)) {
                final ImmutablePair<List<CookedOrder>, List<CookedOrder>> result = shelf.add(orders);
                log.info("Moved {} order from overflow shelf to {}.", result.left.size(), shelf);
            }
        }
    }


    private ShelfTemperatureEnum mapToShelfTemperature(OrderTemperatureEnum orderTemp) {
        ShelfTemperatureEnum shelfTemp = null;
        switch (orderTemp) {
            case hot:
                shelfTemp = Hot;
                break;
            case cold:
                shelfTemp = Cold;
                break;
            case frozen:
                shelfTemp = Frozen;
                break;
        }
        return shelfTemp;
    }

}

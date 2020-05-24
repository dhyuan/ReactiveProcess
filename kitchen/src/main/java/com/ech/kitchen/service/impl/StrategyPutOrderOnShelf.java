package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.mo.ShelfFullException;
import com.ech.kitchen.mo.ShelfTemperatureEnum;
import com.ech.kitchen.service.IShelfSelectStrategy;
import com.ech.order.mo.OrderTemperatureEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
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
            log.info("The {} was put on the {}", cookedOrder, shelf);
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

        if (overflowShelf.availableSpace() == 0) {
            log.info("No cookedOrder is moved to other shelf from overflow shelf. Drop a cookedOrder on overflow shelf randomly");
            overflowShelf.removeOneOrderRandomly();
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
        final Map<OrderTemperatureEnum, List<CookedOrder>> ordersMapOnOverflowShelf =
                overflowShelf.getCookedOrderQueue().stream().collect(Collectors.groupingBy(o -> o.getOrder().getTemp()));

        for (OrderTemperatureEnum orderTemp : ordersMapOnOverflowShelf.keySet()) {
            try {
                final Shelf singleTempShelf = pickupArea.get(mapToShelfTemperature(orderTemp));
                final List<CookedOrder> ordersOnOverflowShelf = ordersMapOnOverflowShelf.get(orderTemp);
                moveOrdersFromOverflowToSingleTemperatureShelf(overflowShelf, singleTempShelf, ordersOnOverflowShelf);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Got error while move orders form overflow to single temperature shelf.", e);
            }
        }
    }

    private void moveOrdersFromOverflowToSingleTemperatureShelf(Shelf overflowShelf, Shelf singleTempShelf,
                                                                List<CookedOrder> ordersOnOverflowShelf) {
        if (CollectionUtils.isEmpty(ordersOnOverflowShelf)) {
            return;
        }
        synchronized (singleTempShelf) {
            synchronized (overflowShelf) {
                final SimpleEntry<List<CookedOrder>, List<CookedOrder>> movedTo = singleTempShelf.add(ordersOnOverflowShelf);
                log.info("Moved {} order from overflow to singleTempShelf {}.", movedTo.getKey(), singleTempShelf);
                if (CollectionUtils.isNotEmpty(movedTo.getKey())) {
                    final SimpleEntry<List<CookedOrder>, List<CookedOrder>> movedFrom = overflowShelf.remove(movedTo.getKey());
                    if (CollectionUtils.isEmpty(movedFrom.getKey()) || movedFrom.getKey().size() != movedTo.getKey().size()) {
                        log.error("There's a transaction problem !!!!!!! {}", movedFrom.getKey());
                    }
                }
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

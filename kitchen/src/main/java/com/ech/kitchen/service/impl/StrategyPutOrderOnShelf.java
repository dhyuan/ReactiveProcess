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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Cold;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Frozen;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Hot;

@Component
@Slf4j
public class StrategyPutOrderOnShelf implements IShelfSelectStrategy {

    final private AtomicLong dropCounter = new AtomicLong(0);

    /**
     * This algorithm works as the following description:
     * 1) Try to place the order on a single temperature shelf based on order temperature.
     * 2) If the single temperature shelf is full then try to place it on overflow shelf.
     * 3) If the overflow shelf is full too, then move an existing order of the overflow to allowable shelf.
     * 4) If all the single temperature is full, a random order from overflow shelf will be dropped and put this one on.
     *
     * @param pickupArea
     * @param cookedOrder
     */
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

    @Override
    public long droppedOrderNumb() {
        return dropCounter.longValue();
    }

    private void putOrderOnOverflowShelf(CookedOrder cookedOrder, Map<ShelfTemperatureEnum, Shelf> pickupArea) {
        Shelf overflowShelf = pickupArea.get(Any);
        synchronized (overflowShelf) {
            try {
                // Try to put order on overflow shelf.
                overflowShelf.add(cookedOrder);
            } catch (ShelfFullException e) {
                log.warn("The overflow shelf is full.");
                // Overflow shelf is full, try to arrange.
                cleanupsOverflowShelfAndTryAgain(cookedOrder, pickupArea, overflowShelf);
            }
        }
    }

    private void cleanupsOverflowShelfAndTryAgain(CookedOrder cookedOrder,
                                                  Map<ShelfTemperatureEnum, Shelf> pickupArea,
                                                  Shelf overflowShelf) {
        // Move one order on overflow to allowable single temperature shelf.
        reassignOrdersOnOverFlowShelf(overflowShelf, pickupArea);

        if (overflowShelf.availableSpace() == 0) {
            log.info("No cookedOrder is moved to other shelf from overflow shelf.");
            log.info("Drop a cookedOrder on overflow shelf randomly");
            final Optional<CookedOrder> droppedOrder = overflowShelf.removeOneOrderRandomly();
            if (droppedOrder.isPresent()) {
                final long droppedNumb = dropCounter.incrementAndGet();
                log.info("{} was removed from overflow shelf. Total dropped number {}", droppedOrder.get(), droppedNumb);
            }
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
                final List<CookedOrder> ordersOnOverflow = ordersMapOnOverflowShelf.get(orderTemp);
                if (CollectionUtils.isEmpty(ordersOnOverflow)) {
                    continue;
                }
                final CookedOrder orderToMove = ordersOnOverflow.get(0);
                final boolean isMovedOk = moveOrdersFromOverflowToSingleTemperatureShelf(
                        overflowShelf, singleTempShelf, orderToMove);
                if (isMovedOk) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Got error while move orders form overflow to single temperature shelf.", e);
            }
        }
    }


    private boolean moveOrdersFromOverflowToSingleTemperatureShelf(Shelf overflowShelf, Shelf singleTempShelf,
                                                                CookedOrder orderToMove) {
        // PutOrderOnOverflowShelf() has already occupy this monitor of overflow shelf.
        // Need to acquire the lock of single temperature shelf.
        // The lock sequence is 'monitor of overflow shelf' --> 'monitor of single temperature shelf'.
        // There is no transactional moving operation from single temperature shelf to overflow shelf.
        // So there's no reverse locking sequence in the system and dead lock will not occur.
        synchronized (singleTempShelf) {
            final boolean isMoveToOk = singleTempShelf.addWithoutException(orderToMove);
            if (isMoveToOk) {
                final boolean isMoveFromOk = overflowShelf.remove(orderToMove);
                if (!isMoveFromOk) {
                    log.error("Transaction problem!");
                    log.error("Order moved to single temperature shelf but failed to removed from overflow shelf.");
                    return false;
                }
                log.info("Moved {} order from overflow to singleTempShelf.", orderToMove);
                return true;
            }
        }
        return false;
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

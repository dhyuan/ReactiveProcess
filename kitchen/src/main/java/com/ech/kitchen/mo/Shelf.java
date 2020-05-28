package com.ech.kitchen.mo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class Shelf {

    @Getter
    private final int maxCapacity;

    @Getter
    private final ShelfTemperatureEnum allowableTemperature;

    @Getter
    private final BlockingQueue<CookedOrder> cookedOrderQueue;


    public Shelf(ShelfTemperatureEnum allowableTemperature, int maxCapacity) {
        this.allowableTemperature = allowableTemperature;
        this.maxCapacity = maxCapacity;
        cookedOrderQueue = new LinkedBlockingQueue<>(maxCapacity);
    }

    public synchronized Optional<CookedOrder> pullOrder() {
        return Optional.ofNullable(cookedOrderQueue.poll());
    }

    public synchronized boolean add(CookedOrder cookedOrder) throws ShelfFullException {
        if (cookedOrderQueue.size() == maxCapacity) {
            String msg = String.format("Exception: Shelf %s is full. Cannot accept %s.",
                    this.allowableTemperature.name(), cookedOrder);
            log.error(msg);
            throw new ShelfFullException(msg);
        }

        final boolean isAdded = cookedOrderQueue.add(cookedOrder);
        setShelfInfoForOrder(cookedOrder, isAdded);
        return isAdded;
    }

    private void setShelfInfoForOrder(CookedOrder cookedOrder, boolean isAdded) {
        if (isAdded) {
            cookedOrder.setShelf(this);
            cookedOrder.setOrderOnShelfTime(Instant.now());
            log.info("Put {} on {}.", cookedOrder, this);
        } else {
            log.error("Cannot put {} on {}.", cookedOrder, this);
        }
    }

    public synchronized boolean addWithoutException(CookedOrder cookedOrder) {
        if (cookedOrderQueue.size() == maxCapacity) {
            log.error("Exception: {} is full. Cannot accept order {}.", this, cookedOrder);
            return false;
        }
        final boolean isAdded = cookedOrderQueue.add(cookedOrder);
        setShelfInfoForOrder(cookedOrder, isAdded);
        return isAdded;
    }

    public synchronized boolean remove(CookedOrder cookedOrder) {
        if (cookedOrderQueue.size() == 0) {
            log.warn("The {} shelf is empty, no cookedOrder available to remove.", allowableTemperature);
            return false;
        }
        final boolean isRemoved = cookedOrderQueue.remove(cookedOrder);
        if (isRemoved) {
            log.info("{} is removed from {}", cookedOrder, this);
        } else {
            log.error("{} cannot be removed from {}", cookedOrder, this);
        }
        return isRemoved;
    }

    public synchronized Optional<CookedOrder> removeOneOrderRandomly() {
        int rndIndex = new Random().nextInt(maxCapacity);
        final Iterator<CookedOrder> iterator = cookedOrderQueue.iterator();
        while (iterator.hasNext()) {
            if (rndIndex-- <= 0) {
                final CookedOrder orderToRemove = iterator.next();
                final boolean isRemoved = cookedOrderQueue.remove(orderToRemove);
                if (isRemoved) {
                    log.info("{} is removed from {}", orderToRemove, this);
                    return Optional.of(orderToRemove);
                }
            }
        }
        return Optional.empty();
    }

    public synchronized int currentOrderNumb() {
        return cookedOrderQueue.size();
    }

    public synchronized int availableSpace() {
        return maxCapacity - cookedOrderQueue.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Shelf: ").append(allowableTemperature.name())
                .append(" availableSpace:").append(availableSpace())
                .append(" usage:").append(currentOrderNumb()).append("/").append(maxCapacity);
        return sb.toString();
    }
}

package com.ech.kitchen.mo;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
                    this.allowableTemperature, cookedOrder);
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
            log.error("Exception: %s is full. Cannot accept order {}.", this, cookedOrder);
            return false;
        }
        final boolean isAdded = cookedOrderQueue.add(cookedOrder);
        setShelfInfoForOrder(cookedOrder, isAdded);
        return isAdded;
    }

    public synchronized SimpleEntry<List<CookedOrder>, List<CookedOrder>> add(List<CookedOrder> cookedOrders) {
        final int availableSpace = availableSpace();
        if (availableSpace == 0) {
            return new SimpleEntry<>(new ArrayList<>(), cookedOrders);
        } else if (availableSpace >= cookedOrders.size()) {
            this.cookedOrderQueue.addAll(cookedOrders);
            return new SimpleEntry(cookedOrders, new ArrayList<>());
        }

        final List<CookedOrder> ordersToAdd = cookedOrders.subList(0, availableSpace);
        final List<CookedOrder> ordersNotAdd = cookedOrders.subList(availableSpace, cookedOrders.size());
        final List<CookedOrder> ordersAdded = new ArrayList<>();
        ordersToAdd.stream().forEach(cookedOrder -> {
            final boolean isAdded = addWithoutException(cookedOrder);
            if (isAdded) {
                ordersAdded.add(cookedOrder);
            } else {
                ordersNotAdd.add(cookedOrder);
            }
        });
        return new SimpleEntry<>(ordersAdded, ordersNotAdd);
    }

    public synchronized SimpleEntry<List<CookedOrder>, List<CookedOrder>> remove(List<CookedOrder> cookedOrders) {
        if (CollectionUtils.isEmpty(cookedOrders)) {
            return null;
        }
        List<CookedOrder> removedOrders = new ArrayList<>();
        List<CookedOrder> unRemovedOrders = new ArrayList<>();
        for (CookedOrder cookedOrder : cookedOrders) {
            final boolean isRemoved = remove(cookedOrder);
            if (isRemoved) {
                removedOrders.add(cookedOrder);
            } else {
                unRemovedOrders.add(cookedOrder);
            }
        }
        return new SimpleEntry(removedOrders, unRemovedOrders);
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

    public synchronized void removeOneOrderRandomly() {
        int rndIndex = new Random().nextInt(maxCapacity);
        final Iterator<CookedOrder> iterator = cookedOrderQueue.iterator();
        while (iterator.hasNext()) {
            if (rndIndex-- <= 0) {
                final CookedOrder orderToRemove = iterator.next();
                cookedOrderQueue.remove(orderToRemove);
                log.info("{} is removed from {}", orderToRemove, this);
                return;
            }
        }
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
                .append(" ").append(currentOrderNumb()).append("/").append(maxCapacity);
        return sb.toString();
    }


    public String detailInfo() {
        StringBuilder sb = new StringBuilder("Shelf ")
                .append(allowableTemperature.name());
        if (CollectionUtils.isEmpty(cookedOrderQueue)) {
            sb.append(" has no order.");
        } else {
            sb.append(" has ").append(cookedOrderQueue.size()).append(" orders.\n");
            for (CookedOrder order : cookedOrderQueue) {
                sb.append("    ");
                sb.append(order.toString());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

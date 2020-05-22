package com.ech.kitchen.mo;

import com.ech.order.mo.Order;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Builder
@Slf4j
@Data
public class Shelf {

    private int maxCapacity;

    private final ShelfTemperatureEnum allowableTemperature;

    private final List<CookedOrder> cookedOrders = new ArrayList<>();

    public synchronized void add(CookedOrder cookedOrder) throws ShelfFullException {
        if (cookedOrders.size() == maxCapacity) {
            String msg = String.format("Exception: %s is full. Cannot accept cookedOrder %s.", this, cookedOrder);
            log.error(msg);
            throw new ShelfFullException(msg);
        }
        cookedOrders.add(cookedOrder);
    }

    public synchronized boolean addWithoutException(CookedOrder order) {
        if (cookedOrders.size() == maxCapacity) {
            log.error("Exception: %s is full. Cannot accept order {}.", this, order);
            return false;
        }
        return cookedOrders.add(order);
    }

    public synchronized ImmutablePair<List<CookedOrder>, List<CookedOrder>> add(List<CookedOrder> cookedOrders) {
        final int availableSpace = maxCapacity - this.cookedOrders.size();
        if (availableSpace >= cookedOrders.size()) {
            this.cookedOrders.addAll(cookedOrders);
            return new ImmutablePair(cookedOrders, Lists.newArrayList());
        }

        final List<CookedOrder> ordersToAdd = cookedOrders.subList(0, availableSpace);
        final List<CookedOrder> ordersNotAdd = cookedOrders.subList(availableSpace, cookedOrders.size());
        this.cookedOrders.addAll(ordersToAdd);
        return new ImmutablePair<>(ordersToAdd, ordersNotAdd);
    }

    public synchronized void remove(CookedOrder cookedOrder) {
        if (cookedOrders.size() == 0) {
            log.warn("The {} shelf is empty, no cookedOrder available to remove.", allowableTemperature);
            return;
        }
        cookedOrders.remove(cookedOrder);
    }

    public synchronized void remove(int index) {
        if (index < 0 || index >= maxCapacity) {
            log.error("Not right index {} for {}", index, this);
            return;
        }
        cookedOrders.remove(index);
    }


    public synchronized int currentOrderNumb() {
        return cookedOrders.size();
    }

    public synchronized int availableSpace() {
        return maxCapacity - cookedOrders.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Shelf: ").append(allowableTemperature.name())
                .append(" maxCapacity ").append(maxCapacity);
        return sb.toString();
    }


    public String detailInfo() {
        StringBuilder sb = new StringBuilder("Shelf ")
                .append(allowableTemperature.name());
        if (CollectionUtils.isEmpty(cookedOrders)) {
            sb.append(" has no order.");
        } else {
            sb.append(" has ").append(cookedOrders.size()).append(" orders.\n");
            for (CookedOrder order : cookedOrders) {
                sb.append("    ");
                sb.append(order.toString());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}

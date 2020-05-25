package com.ech.kitchen.mo;

import com.ech.order.mo.Order;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;

@Slf4j
public class CookedOrder {
    @Getter @Setter
    private Order order;

    @Getter @Setter
    private Instant receivedByKitchenTime;

    @Getter @Setter
    private Instant cookedDoneTime;

    @Getter @Setter
    private Instant orderOnShelfTime;

    @Getter @Setter
    private Shelf shelf;

    @Getter @Setter
    private Instant deliveredTime;

    public CookedOrder() {
    }

    public CookedOrder(Order order) {
        this.order = order;
    }

    public Long getOrderAge() {
        if (orderOnShelfTime == null) {
            return null;
        }
        // Do not change orderAge if the order has already been delivered.
        final Instant currentTime = deliveredTime == null ? Instant.now() : deliveredTime;
        final Duration timeOnShelf = Duration.between(orderOnShelfTime, currentTime);
        return timeOnShelf.toSeconds();
    }

    public Optional<Float> getOrderValue() {
        return calculateOrderOnShelfTTL(this);
    }

    public static Optional<Float> calculateOrderOnShelfTTL(CookedOrder cookedOrder) {
        final Integer shelfLife = cookedOrder.getOrder().getShelfLife();
        final Float decayRate = cookedOrder.getOrder().getDecayRate();
        final Shelf shelf = cookedOrder.getShelf();
        final Long orderAge = cookedOrder.getOrderAge();

        if (shelf == null || orderAge == null) {
            log.debug("The order is not on the shelf yet. No TTL value");
            return Optional.empty();
        }

        final float ttl = (shelfLife - decayRate * orderAge * getShelfDecayModifier(shelf)) / shelfLife;
        return Optional.of(ttl);
    }

    private static int getShelfDecayModifier(Shelf shelf) {
        return shelf.getAllowableTemperature() == Any ? 2 : 1;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("CookedOrder: (").append(order).append(") ");
        if (shelf != null && orderOnShelfTime != null) {
            sb.append(" onShelf=").append(shelf.getAllowableTemperature());
            sb.append(" orderAge=").append(getOrderAge());
        }
        if (orderOnShelfTime != null) {
            sb.append(" orderOnShelfTime=").append(orderOnShelfTime);
        }
        if (deliveredTime != null) {
            sb.append(" deliveredTime=").append(deliveredTime);
        }
        sb.append(" orderValue=").append(getOrderValue());
        return sb.toString();
    }
}

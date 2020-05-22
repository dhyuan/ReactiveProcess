package com.ech.kitchen.mo;

import com.ech.order.mo.Order;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;

@Data
@Slf4j
public class CookedOrder {

    private Order order;

    private Instant receivedByKitchenTime;

    private Instant cookedDoneTime;

    private Instant orderOnShelfTime;

    public CookedOrder() {
    }

    public CookedOrder(Order order) {
        this.order = order;
    }

    public Long getOrderAge() {
        if (orderOnShelfTime == null) {
            log.warn("There is no orderOnShelfTime.");
            return Long.MIN_VALUE;
        }

        final Duration timeOnShelf = Duration.between(Instant.now(), orderOnShelfTime);
        return timeOnShelf.toSeconds();
    }
}

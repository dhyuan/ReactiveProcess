package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.order.mo.Order;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Hot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderOnShelfTTLCalculatorTest {

    private static final Shelf SINGLE_TEMPERATURE_SHELF = new Shelf(Hot, 10);
    private static final Shelf OVERFLOW_SHELF = new Shelf(Any, 10);

    @Test
    public void testOrderValueOnHotShelf() {
        int shelfLif = 300;
        float decayRate = 0.5f;
        long orderAge = 600;

        // (300 - 0.5 * 600 * 1) / 300 = 0
        final CookedOrder cookedOrder = mockOrder(shelfLif, decayRate, orderAge, SINGLE_TEMPERATURE_SHELF);
        assertEquals(Optional.of(0f), CookedOrder.calculateOrderOnShelfTTL(cookedOrder));
    }

    @Test
    public void testOrderValueOnOverflowShelf() {
        int shelfLif = 300;
        float decayRate = 0.5f;
        long orderAge = 600;

        // (300 - 0.5 * 600 * 2) / 300 = -1
        final CookedOrder cookedOrder = mockOrder(shelfLif, decayRate, orderAge, OVERFLOW_SHELF);
        assertEquals(Optional.of(-1f), CookedOrder.calculateOrderOnShelfTTL(cookedOrder));
    }

    @Test
    public void testOrderValuePositive() {
        int shelfLif = 300;
        float decayRate = 0.5f;
        long orderAge = 100;

        // (300 - 0.45 * 100 * 1) / 300 = 0.6666666865348816
        final CookedOrder cookedOrder = mockOrder(shelfLif, decayRate, orderAge, SINGLE_TEMPERATURE_SHELF);
        assertEquals(Optional.of(0.8333333f), CookedOrder.calculateOrderOnShelfTTL(cookedOrder));
    }

    private CookedOrder mockOrder(int shelfLif, float decayRate, long orderAge, Shelf shelf) {
        final Order order = mock(Order.class);
        final CookedOrder cookedOrder = mock(CookedOrder.class);

        when(cookedOrder.getOrder()).thenReturn(order);
        when(cookedOrder.getOrderAge()).thenReturn(orderAge);
        when(cookedOrder.getShelf()).thenReturn(shelf);

        when(order.getDecayRate()).thenReturn(decayRate);
        when(order.getShelfLife()).thenReturn(shelfLif);

        return cookedOrder;
    }
}

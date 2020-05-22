package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.service.IOrderOnShelfTTLCalculateStrategy;
import com.ech.order.mo.Order;
import org.junit.jupiter.api.Test;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Cold;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Hot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderOnShelfTTLCalculatorTest {

    private static final float PRECISION = 0.0000001f;

    @Test
    public void testOrderValueOnHotShelf() {
        IOrderOnShelfTTLCalculateStrategy calculator = new OrderOnShelfTTLCalculator();

        int shelfLif = 300;
        float decayRate = 0.5f;
        long orderAge = 600;
        final Shelf hotShelf = Shelf.builder().allowableTemperature(Hot).build();

        // (300 - 0.5 * 600 * 1) / 300 = 0
        final float orderValue = calculateOrderValue(calculator, shelfLif, decayRate, orderAge, hotShelf);
        assertEquals(0, orderValue);
    }

    @Test
    public void testOrderValueOnOverflowShelf() {
        IOrderOnShelfTTLCalculateStrategy calculator = new OrderOnShelfTTLCalculator();

        int shelfLif = 300;
        float decayRate = 0.5f;
        long orderAge = 600;
        final Shelf hotShelf = Shelf.builder().allowableTemperature(Any).build();

        // (300 - 0.5 * 600 * 2) / 300 = -1
        final float orderValue = calculateOrderValue(calculator, shelfLif, decayRate, orderAge, hotShelf);
        assertEquals(-1, orderValue);
    }

    @Test
    public void testOrderValuePositive() {
        IOrderOnShelfTTLCalculateStrategy calculator = new OrderOnShelfTTLCalculator();

        int shelfLif = 300;
        float decayRate = 0.5f;
        long orderAge = 100;
        final Shelf hotShelf = Shelf.builder().allowableTemperature(Any).build();

        // (300 - 0.45 * 100 * 2) / 300 = 0.6666666865348816
        final float orderValue = calculateOrderValue(calculator, shelfLif, decayRate, orderAge, hotShelf);
        assertEquals(0.6666666865348816f, orderValue);
    }

    private float calculateOrderValue(IOrderOnShelfTTLCalculateStrategy calculator, int shelfLif, float decayRate, long orderAge, Shelf hotShelf) {
        final Order order = mock(Order.class);
        final CookedOrder cookedOrder = mock(CookedOrder.class);

        when(cookedOrder.getOrder()).thenReturn(order);
        when(cookedOrder.getOrderAge()).thenReturn(orderAge);

        when(order.getDecayRate()).thenReturn(decayRate);
        when(order.getShelfLife()).thenReturn(shelfLif);

        return calculator.calculateOrderOnShelfTTL(cookedOrder, hotShelf);
    }
}

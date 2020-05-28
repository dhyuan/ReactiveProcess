package com.ech.order.mo;

import org.junit.jupiter.api.Test;

import static com.ech.order.mo.OrderTemperatureEnum.cold;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

    @Test
    public void testOrderConstructor() {
        final String orderName = "orderOne";
        final Order order = new Order(orderName, cold);

        assertEquals(orderName, order.getName());
        assertEquals(cold, order.getTemp());
    }
}

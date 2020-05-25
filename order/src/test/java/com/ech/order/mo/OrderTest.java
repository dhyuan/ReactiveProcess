package com.ech.order.mo;

import static com.ech.order.mo.OrderTemperatureEnum.cold;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

    public void testOrderConstructor() {
        final String orderName = "orderOne";
        final Order order = new Order(orderName, cold);

        assertEquals(orderName, order.getName());
        assertEquals(cold, order.getTemp());
    }
}

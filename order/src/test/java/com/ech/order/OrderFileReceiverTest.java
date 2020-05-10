package com.ech.order;

import com.ech.order.impl.OrderFileScanner;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderFileReceiverTest {

    final private static String ORDERS_JSON_FILENAME = "orders_5.json";

    @Test
    public void testReadOrdersFromFileTest() throws URISyntaxException {
        IOrderScanner orderReceiver = new OrderFileScanner(ORDERS_JSON_FILENAME);

        final List<Order> orders = orderReceiver.readAllOrders();
        assertNotNull(orders);
        assertEquals(5, orders.size(), "The size of order list is not right.");

        final Order firstOrder = orders.get(0);
        assertTrue("a8cfcb76-7f24-4420-a5ba-d46dd77bdffd".equals(firstOrder.getId()));
        assertTrue("Banana Split".equals(firstOrder.getName()), "");
        assertEquals(TemperatureEnum.frozen, firstOrder.getTemp());
        assertEquals(20, (int) firstOrder.getShelfLife());
        assertEquals(0.63f, firstOrder.getDecayRate());

        Order lastOrder = orders.get(3);
        assertTrue("690b85f7-8c7d-4337-bd02-04e04454c826".equals(lastOrder.getId()));
        assertTrue("Yogurt".equals(lastOrder.getName()), "");
        assertEquals(TemperatureEnum.cold, lastOrder.getTemp());
        assertEquals(263, (int) lastOrder.getShelfLife());
        assertEquals(0.37f, lastOrder.getDecayRate());
    }

}

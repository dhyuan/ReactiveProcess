package com.ech.order.impl;

import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.mo.Order;
import com.ech.order.mo.TemperatureEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class OrderFileScannerTest {

    final private static String ORDERS_JSON_FILENAME = "orders_5.json";


    @Test
    public void testRegisterOrderObserver() {
        IOrderScanner orderScanner = new OrderFileScanner(ORDERS_JSON_FILENAME);

        final IOrderObserver firstObserver = mock(IOrderObserver.class);
        orderScanner.registerOrderObserver(firstObserver);
        assertEquals(1, orderScanner.getAllOrderObserver().size());
    }

    @Test
    public void testUnRegisterOrderObserver() {
        IOrderScanner orderScanner = new OrderFileScanner(ORDERS_JSON_FILENAME);

        final IOrderObserver firstObserver = mock(IOrderObserver.class);
        final IOrderObserver secondObserver = mock(IOrderObserver.class);
        orderScanner.registerOrderObserver(firstObserver);
        orderScanner.registerOrderObserver(secondObserver);
        assertEquals(2, orderScanner.getAllOrderObserver().size());

        orderScanner.unRegisterOrderObserver(secondObserver);
        assertEquals(1, orderScanner.getAllOrderObserver().size());
    }

    @Test
    public void testParseOrdersFromFile() throws URISyntaxException {
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

    @Test
    public void testAbsoluteFilePath() throws URISyntaxException {
        final String filePath = new File("").getAbsolutePath() + "/src/test/resources/orders_5.json";
        log.info("filePath = " + filePath);

        IOrderScanner orderReceiver = new OrderFileScanner(filePath);
        final List<Order> orders = orderReceiver.readAllOrders();
        assertNotNull(orders);
        assertEquals(5, orders.size(), "The size of order list is not right.");
    }

}

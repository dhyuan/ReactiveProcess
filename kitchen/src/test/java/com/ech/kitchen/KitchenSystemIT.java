package com.ech.kitchen;

import com.ech.kitchen.service.IKitchenService;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
public class KitchenSystemIT extends KitchenBaseIT {

    @Autowired
    public IOrderScanner orderScanner;

    @Autowired
    public IOrderObserver kitchenOrderReceiver;

    @Autowired
    public IKitchenService kitchenService;

    private final static int EXPECTED_ORDER_AMOUNT = 132;

    @Test
    public void testSubscribe() throws InterruptedException {
        orderScanner.registerOrderObserver(kitchenOrderReceiver);
        orderScanner.startOrderScanner();
        kitchenService.openKitchen(kitchenOrderReceiver);

        int usedTime = 0;
        int expectedMaxProcessTime = 8000;
        while (kitchenService.processedOrderAmount() < EXPECTED_ORDER_AMOUNT) {
            Thread.sleep(500);
            expectedMaxProcessTime += usedTime;
        }
        kitchenService.closeKitchen();

        assertEquals(EXPECTED_ORDER_AMOUNT, kitchenService.processedOrderAmount());
        assertTrue(usedTime < expectedMaxProcessTime);
    }
}

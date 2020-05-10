package com.ech.kitchen;

import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KitchenTestApp.class)
@Slf4j
public class KitchenSystemIT {

    @Autowired
    public IOrderScanner orderScanner;

    @Autowired
    public IOrderObserver kitchenOrderReceiver;

    @Autowired
    public IKitchenSystem kitchenSystem;

    private final static int EXPECTED_ORDER_AMOUNT = 132;

    @Test
    public void testSubscribe() throws InterruptedException {
        orderScanner.registerOrderReceiver(kitchenOrderReceiver);
        kitchenSystem.openKitchen(kitchenOrderReceiver);

        int usedTime = 0;
        int expectedMaxProcessTime = 8000;
        while (kitchenSystem.processedOrderAmount() < EXPECTED_ORDER_AMOUNT) {
            Thread.sleep(500);
            expectedMaxProcessTime += usedTime;
        }
        kitchenSystem.closeKitchen();

        Assert.assertEquals(EXPECTED_ORDER_AMOUNT, kitchenSystem.processedOrderAmount());
        Assert.assertTrue(usedTime < expectedMaxProcessTime);
    }
}

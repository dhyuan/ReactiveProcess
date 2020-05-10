package com.ech.kitchen;

import com.ech.kitchen.impl.KitchenOrderReceiver;
import com.ech.kitchen.impl.KitchenSystem;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.impl.OrderFileScanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = KitchenTestApp.class)
//@Import({
//        OrderFileScanner.class,
//        KitchenOrderReceiver.class,
//        KitchenSystem.class
//})
public class KitchenSystemTest {
    private static final Logger LOG = LogManager.getLogger(KitchenSystemTest.class);

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
        int expectedMaxProcessTime = 5000;
        while (kitchenSystem.processedOrderAmount() < EXPECTED_ORDER_AMOUNT) {
            Thread.sleep(100);
            expectedMaxProcessTime += usedTime;
        }

        kitchenSystem.closeKitchen();

        Assert.assertEquals(EXPECTED_ORDER_AMOUNT, kitchenSystem.processedOrderAmount());
        Assert.assertTrue(usedTime < expectedMaxProcessTime);
    }
}

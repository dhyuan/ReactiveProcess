package com.ech.kitchen;

import com.ech.kitchen.service.ICookedOrderPickStrategy;
import com.ech.kitchen.service.IKitchenService;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
        final int ingestionRate = 60;

        orderScanner.setIngestionRate(ingestionRate);  // set ingestion rate as 10 millisecond to seed up the test.
        orderScanner.registerOrderObserver(kitchenOrderReceiver);
        orderScanner.startOrderScanner();

        kitchenService.openKitchen(kitchenOrderReceiver);

        Semaphore watchProcess = new Semaphore(0);
        checkProcessedAmount(watchProcess);
        final int maxWaitTime = ingestionRate * EXPECTED_ORDER_AMOUNT + 10000;
        watchProcess.tryAcquire(maxWaitTime, TimeUnit.MILLISECONDS);

        log.info("The oder processed {}", kitchenService.processedOrderAmount());

        kitchenService.closeKitchen();

        assertEquals(EXPECTED_ORDER_AMOUNT, kitchenService.processedOrderAmount());
    }

    private void checkProcessedAmount(Semaphore watchProcess) {
        new Thread(() -> {
            while (kitchenService.processedOrderAmount() < EXPECTED_ORDER_AMOUNT) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.info(e.getMessage());
                }
            }
            watchProcess.release(1);
        }).start();
    }

}

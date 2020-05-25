package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.Kitchen;
import com.ech.kitchen.service.ICourierService;
import com.ech.kitchen.service.IExpiredOrderCheckingService;
import com.ech.kitchen.service.IKitchenService;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {CssApplicationTestConfig.class})
@TestPropertySource("classpath:kitchen-service-order-expire-test.properties")
@Slf4j
public class KitchenServiceOrderExpireIT extends BaseIT {

    @Autowired
    private IOrderScanner orderScanner;

    @Autowired
    private IOrderObserver kitchenOrderReceiver;

    @Autowired
    private IKitchenService kitchenService;

    @Autowired
    private ICourierService courierService;

    @Autowired
    private IExpiredOrderCheckingService expiredOrderCheckingService;

    @Autowired
    private Kitchen kitchen;

    private final long ORDER_INGESTION_RATE = 200;   // millisecond
    private final static String JSON_FILE = "orders_expire_4.json";
    private final static int EXPECTED_ORDER_AMOUNT = 4;

    private final int MAX_REDUNDANCY_TIME = 10000;

    final BooleanSupplier endCondition = () ->
            kitchen.getTotalNumberOfOrderOnShelf() == 0 && kitchenService.totalIncomingOrderNumb() >= EXPECTED_ORDER_AMOUNT;

    /**
     * There are four orders are defined in order_expire_4.json.
     * The test setting as the following:
     * <p>
     * 1) The orders's max age is 2 seconds.
     * 2) The expire order checking service period is set as 0.5 seconds
     * 3) The courier service period are set more than 8 seconds.
     * 4) The shelf's capacity big enough to hold all the orders.
     * <p>
     * All of the four orders are expected expired after 3 seconds.
     */
    @Test
    public void testExpiredOrderProcess() {
        orderScanner.setOrderFile(JSON_FILE);
        orderScanner.setIngestionRate(ORDER_INGESTION_RATE);
        orderScanner.registerOrderObserver(kitchenOrderReceiver);
        orderScanner.startOrderScanner();

        kitchenService.openKitchen(kitchenOrderReceiver);
        courierService.start();

        final long maxWaitTime = orderScanner.getIngestionRate() * EXPECTED_ORDER_AMOUNT + MAX_REDUNDANCY_TIME;
        waitToBeDone(endCondition, maxWaitTime);
        log.info("Total {} orders are processed.", kitchenService.totalIncomingOrderNumb());

        assertEquals(EXPECTED_ORDER_AMOUNT, kitchenService.totalIncomingOrderNumb());

        final long totalDeliveredOrderNumb = courierService.deliveryCount();
        log.info("totalDeliveredOrderNumb={}", totalDeliveredOrderNumb);
        assertEquals(0, totalDeliveredOrderNumb);

        final long totalExpiredOrderNumb = expiredOrderCheckingService.totalExpiredOrderNumb();
        final long totalFailedCleanNumb = expiredOrderCheckingService.totalFailedCleanNumb();
        assertEquals(EXPECTED_ORDER_AMOUNT, totalExpiredOrderNumb);
        assertEquals(0, totalFailedCleanNumb);

        courierService.stop();
        kitchenService.closeKitchen();
    }
}
package com.ech.kitchen.service.impl;

import com.ech.kitchen.courier.service.ICourierService;
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
@TestPropertySource("classpath:kitchen-service-delivery-test.properties")
@Slf4j
public class KitchenServiceCourierDeliveryIT extends BaseIT {

    @Autowired
    private IOrderScanner orderScanner;

    @Autowired
    private IOrderObserver kitchenOrderReceiver;

    @Autowired
    private IKitchenService kitchenService;

    @Autowired
    private ICourierService courierService;

    private final long ORDER_INGESTION_RATE = 200;   // millisecond
    private final static String JSON_FILE_3 = "orders_delivery_3.json";
    private final static int EXPECTED_ORDER_AMOUNT = 3;

    private final int MAX_REDUNDANCY_TIME = 10000;

    /**
     * There are three orders are defined in order_delivery_3.json.
     * The test setting as the following:
     * <p>
     * 1) The courier request period time is set no more than 1 second in kitchen-service-delivery-test.properties.
     * 2) Each order's expire time(3 seconds) is long enough for courier to pick up.
     * 3) Order ingestion rate is 0.5 seconds.
     * <p>
     * All the three orders are expected delivered by courier service.
     */
    @Test
    public void testCookedOrderDelivery() {
        orderScanner.setOrderFile(JSON_FILE_3);
        orderScanner.setIngestionRate(ORDER_INGESTION_RATE);
        orderScanner.registerOrderObserver(kitchenOrderReceiver);
        orderScanner.startOrderScanner();

        assertEquals(0, kitchenService.totalIncomingOrderNumb());
        kitchenService.openKitchen(kitchenOrderReceiver);

        assertEquals(0, courierService.deliveryCount());
        courierService.start();

        final long maxWaitTime = orderScanner.getIngestionRate() * EXPECTED_ORDER_AMOUNT + MAX_REDUNDANCY_TIME;
        final BooleanSupplier endCondition = () -> courierService.deliveryCount() >= EXPECTED_ORDER_AMOUNT;

        waitToBeDone(endCondition, maxWaitTime);
        log.info("Total {} orders are processed.", kitchenService.totalIncomingOrderNumb());

        assertEquals(EXPECTED_ORDER_AMOUNT, kitchenService.totalIncomingOrderNumb());
        assertEquals(EXPECTED_ORDER_AMOUNT, courierService.deliveryCount());

        courierService.stop();
        kitchenService.closeKitchen();
    }
}

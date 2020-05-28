package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.Kitchen;
import com.ech.kitchen.courier.service.ICourierService;
import com.ech.kitchen.service.IExpiredOrderCheckingService;
import com.ech.kitchen.service.IKitchenService;
import com.ech.kitchen.service.IShelfSelectStrategy;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The kitchen-service-test.properties defines all the runtime parameters of this test case.
 * You can change parameters in that file to change the system behavior.
 *
 * This test case will do be verification based on the
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {CssApplicationTestConfig.class})
@TestPropertySource("classpath:kitchen-service-test.properties")
@Slf4j
public class KitchenSystemIT extends BaseIT {

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
    private IShelfSelectStrategy orderOnShelfStrategy;

    @Autowired
    private Kitchen kitchen;

    @Test
    @Tag("MassDataTest")
    public void testFullScenario() {
        // NOTE: this EXPECTED_ORDER_AMOUNT value should consistent to the record number in your orders json file !
        final int EXPECTED_ORDER_AMOUNT = 132;

        orderScanner.registerOrderObserver(kitchenOrderReceiver);
        orderScanner.startOrderScanner();

        kitchenService.openKitchen(kitchenOrderReceiver);
        courierService.start();

        final BooleanSupplier endCondition = () -> kitchenService.totalIncomingOrderNumb() >= EXPECTED_ORDER_AMOUNT;
        final int MAX_REDUNDANCY_TIME = 10000;
        final long maxWaitTime = orderScanner.getIngestionRate() * EXPECTED_ORDER_AMOUNT + MAX_REDUNDANCY_TIME;
        waitToBeDone(endCondition, maxWaitTime);

        log.info("Total {} orders received by kitchen.", kitchenService.totalIncomingOrderNumb());
        assertEquals(EXPECTED_ORDER_AMOUNT, kitchenService.totalIncomingOrderNumb());

        final long totalOrderDeliveredSuccessNumb = courierService.deliveryCount();
        final long totalOrderDeliveryErrorNumb = courierService.deliveryErrorCount();
        log.info("totalOrderDeliveredSuccessNumb={}, totalOrderDeliveryErrorNumb={}",
                totalOrderDeliveredSuccessNumb, totalOrderDeliveryErrorNumb);

        final long totalExpiredOrderNumb = expiredOrderCheckingService.totalExpiredOrderNumb();
        final long totalFailedCleanNumb = expiredOrderCheckingService.totalFailedCleanNumb();
        log.info("totalExpiredOrderNumb={}, totalFailedCleanNumb={}", totalExpiredOrderNumb, totalFailedCleanNumb);

        final long numberOfOrderStillOnShelf = kitchen.getTotalNumberOfOrderOnShelf();
        log.info("numberOfOrderStillOnShelf={}", numberOfOrderStillOnShelf);

        final long totalDroppedNumb = orderOnShelfStrategy.droppedOrderNumb();
        log.info("totalDroppedNumb={}", totalDroppedNumb);

        final long totalRecordNumb = totalOrderDeliveredSuccessNumb + totalOrderDeliveryErrorNumb
                + totalExpiredOrderNumb + totalFailedCleanNumb
                + numberOfOrderStillOnShelf
                + totalDroppedNumb;
        log.info("totalRecordNumb={},", totalRecordNumb);
        assertEquals(EXPECTED_ORDER_AMOUNT, totalRecordNumb);

        assertEquals(0, totalFailedCleanNumb);
        assertEquals(0, totalOrderDeliveryErrorNumb);

        courierService.stop();
        kitchenService.closeKitchen();
    }

}

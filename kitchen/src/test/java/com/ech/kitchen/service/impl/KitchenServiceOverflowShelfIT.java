package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.Kitchen;
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

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Cold;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Hot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {CssApplicationTestConfig.class})
@TestPropertySource("classpath:kitchen-service-overflow-test.properties")
@Slf4j
public class KitchenServiceOverflowShelfIT extends BaseIT {

    @Autowired
    public IOrderScanner orderScanner;

    @Autowired
    public IOrderObserver kitchenOrderReceiver;

    @Autowired
    public IKitchenService kitchenService;

    @Autowired
    private ICourierService courierService;

    @Autowired
    private Kitchen kitchen;

    private final long ORDER_INGESTION_RATE = 100;   // millisecond
    private final static String JSON_FILE_OVERFLOW_6 = "orders_overflow_6.json";
    private final static int EXPECTED_ORDER_AMOUNT = 6;

    private final int MAX_REDUNDANCY_TIME = 10000;



    /**
     * Test Data Preparation:
     * 1) There are six orders are defined in order_overflow_6.json.
     *    And five of them are cold orders whose orderAge are set more than 10 seconds.
     * 2) The cold and overflow shelf capacity both are set as 2 in kitchen-service-overflow-test.properties.
     * 3) The courier service's work period is set as 20 seconds to avoid pull order from shelves.
     * 4) The expire order check service's work period is set as 20 seconds to avoid drop out order from shelves
     *
     * Expected behavior:
     * 1) The cold shelf can only hold two orders and three orders will be moved to overflow shelf.
     * 2) Overflow shield can only hold two orders of the three
     * 3) One order will be thrown.
     */
    @Test
    public void testCookedOrderOverflow() {
        orderScanner.setOrderFile(JSON_FILE_OVERFLOW_6);
        orderScanner.setIngestionRate(ORDER_INGESTION_RATE);
        orderScanner.registerOrderObserver(kitchenOrderReceiver);
        orderScanner.startOrderScanner();

        kitchenService.openKitchen(kitchenOrderReceiver);
        courierService.start();

        final long maxWaitTime = orderScanner.getIngestionRate() * EXPECTED_ORDER_AMOUNT + MAX_REDUNDANCY_TIME;
        final BooleanSupplier endCondition = () -> kitchenService.totalIncomingOrderNumb() >= EXPECTED_ORDER_AMOUNT;

        waitToBeDone(endCondition, maxWaitTime);
        log.info("Total {} orders are processed.", kitchenService.totalIncomingOrderNumb());

        assertEquals(0, courierService.deliveryCount());
        assertEquals(EXPECTED_ORDER_AMOUNT, kitchenService.totalIncomingOrderNumb());

        assertEquals(5, kitchen.getTotalNumberOfOrderOnShelf());

        final Set<String> orderIdsInColdShelf = kitchen.getPickupArea().get(Cold).getCookedOrderQueue()
                .stream().map(co -> co.getOrder().getId()).collect(Collectors.toSet());
        assertEquals(2, orderIdsInColdShelf.size());
        // The first two cold orders are saved on cold shelf.
        assertTrue(orderIdsInColdShelf.contains("order_6_1"));
        assertTrue(orderIdsInColdShelf.contains("order_6_2"));

        final Set<String> orderIdsInHotShelf = kitchen.getPickupArea().get(Hot).getCookedOrderQueue()
                .stream().map(co -> co.getOrder().getId()).collect(Collectors.toSet());
        assertTrue(orderIdsInHotShelf.contains("order_6_3"));

        final Set<String> orderIdsInOverflowShelf = kitchen.getPickupArea().get(Any).getCookedOrderQueue()
                .stream().map(co -> co.getOrder().getId()).collect(Collectors.toSet());
        assertEquals(2, orderIdsInOverflowShelf.size());
        // The last cold order will always on overflow shelf.
        assertTrue(orderIdsInOverflowShelf.contains("order_6_6"));

        // The order 'order_6_5' or 'order_6_4' was dropped randomly.
        assertTrue(orderIdsInOverflowShelf.contains("order_6_5")
                || orderIdsInOverflowShelf.contains("order_6_4"));

        courierService.stop();
        kitchenService.closeKitchen();
    }
}

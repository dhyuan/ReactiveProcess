package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.Kitchen;
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

import static com.ech.kitchen.mo.ShelfTemperatureEnum.Any;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Cold;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Frozen;
import static com.ech.kitchen.mo.ShelfTemperatureEnum.Hot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {CssApplicationTestConfig.class})
@TestPropertySource("classpath:kitchen-service-order-subscribe-test.properties")
@Slf4j
public class KitchenServiceOrderSubscribeIT extends BaseIT {

    @Autowired
    public IOrderScanner orderScanner;

    @Autowired
    public IOrderObserver kitchenOrderReceiver;

    @Autowired
    public IKitchenService kitchenService;

    @Autowired
    private Kitchen kitchen;

    private final static String JSON_FILE_WITH_5_RECODES = "orders_5.json";
    private final static int EXPECTED_ORDER_AMOUNT = 5;
    private final int MAX_REDUNDANCY_TIME = 10000;

    @Test
    public void testCreateKitchen() {
        assertEquals(4, kitchen.getPickupArea().values().size());

        assertEquals(10, kitchen.getPickupArea().get(Hot).getMaxCapacity());
        assertEquals(10, kitchen.getPickupArea().get(Cold).getMaxCapacity());
        assertEquals(10, kitchen.getPickupArea().get(Frozen).getMaxCapacity());
        assertEquals(15, kitchen.getPickupArea().get(Any).getMaxCapacity());
    }

    @Test
    public void testSubscribe() throws InterruptedException {
        orderScanner.setOrderFile(JSON_FILE_WITH_5_RECODES);
        orderScanner.registerOrderObserver(kitchenOrderReceiver);
        orderScanner.startOrderScanner();

        kitchenService.openKitchen(kitchenOrderReceiver);

        final long maxWaitTime = orderScanner.getIngestionRate() * EXPECTED_ORDER_AMOUNT + MAX_REDUNDANCY_TIME;
        waitToBeDone(() -> kitchenService.totalIncomingOrderNumb() >= EXPECTED_ORDER_AMOUNT, maxWaitTime);
        log.info("{} orders are processed.", kitchenService.totalIncomingOrderNumb());

        kitchenService.closeKitchen();

        assertEquals(EXPECTED_ORDER_AMOUNT, kitchenService.totalIncomingOrderNumb());
    }

}

package com.ech.order.impl;

import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.mo.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.reactivestreams.Subscription;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ech.order.impl.ExpectedOrderData.ORDER_1_ID;
import static com.ech.order.impl.ExpectedOrderData.ORDER_1_NAME;
import static com.ech.order.impl.ExpectedOrderData.ORDER_2_NAME;
import static com.ech.order.impl.ExpectedOrderData.ORDER_2_TEMP;
import static com.ech.order.impl.ExpectedOrderData.ORDER_5_NAME;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@SpringBootConfiguration
@SpringBootTest
public class OrderFileScannerIT {

    final private static String ORDERS_JSON_FILENAME = "orders_5.json";
    final private static int TEST_CASE_TIMEOUT = 10;    // Test case timeout setting. second.
    final private static int ORDERS_NUMBER_IN_JSON = 5; // The number of order data in order_5.json.

    final private static int TWO_SECONDS = 2000;
    final private static int THREE_SECONDS = 3000;
    @Test
    public void testOrderIngestionRateAtOneSecond() {
        final IOrderScanner orderScanner = new OrderFileScanner(ORDERS_JSON_FILENAME);
        orderScanner.setIngestionRate(TWO_SECONDS);

        // There are 5 orders, one order needs 2 seconds.  Total time is 10 seconds.
        StepVerifier.withVirtualTime(() -> orderScanner.readOrderAsFlux())
                .thenAwait(ofSeconds(2))
                .expectNextCount(1)
                .thenAwait(ofSeconds(4))
                .expectNextCount(2)
                .thenAwait(ofSeconds(4))
                .expectNextCount(2)
                .expectComplete()
                .verify();
    }

    @Test
    public void testOrderIngestionRateAtThreeSecond() {
        final IOrderScanner orderScanner = new OrderFileScanner(ORDERS_JSON_FILENAME);
        orderScanner.setIngestionRate(THREE_SECONDS);
        // There are 5 orders, one order needs 3 seconds.  Total time is 15 seconds.
        StepVerifier.withVirtualTime(() -> orderScanner.readOrderAsFlux())
                .thenAwait(ofSeconds(3))
                .expectNextCount(1)
                .thenAwait(ofSeconds(3))
                .expectNextCount(1)
                .thenAwait(ofSeconds(9))
                .expectNextCount(3)
                .expectComplete()
                .verify();
    }

    @Test
    public void testOrderNotification() throws InterruptedException {
        final IOrderScanner orderScanner = new OrderFileScanner(ORDERS_JSON_FILENAME);
        final CountDownLatch countDownLatch = new CountDownLatch(ORDERS_NUMBER_IN_JSON);
        final IOrderObserver<Order> observer = mock(IOrderObserver.class);

        doAnswer((Answer<Void>) invocation -> {
            Subscription subscription = invocation.getArgument(0);
            subscription.request(ORDERS_NUMBER_IN_JSON);
            return null;
        }).when(observer).onSubscribe(any(Subscription.class));

        final int[] count = new int[1];
        doAnswer((Answer<Void>) invocation -> {
            count[0]++;
            Order order = invocation.getArgument(0);
            switch (count[0]) {
                case 1:
                    assertEquals(ORDER_1_ID, order.getId());
                    assertEquals(ORDER_1_NAME, order.getName());
                    break;
                case 2:
                    assertEquals(ORDER_2_NAME, order.getName());
                    assertEquals(ORDER_2_TEMP, order.getTemp().name());
                    break;
                case 5:
                    assertEquals(ORDER_5_NAME, order.getName());
                    break;
            }

            countDownLatch.countDown();
            return null;
        }).when(observer).onNext(any(Order.class));

        orderScanner.registerOrderObserver(observer);
        orderScanner.startOrderScanner();
        observer.beginObserve();

        countDownLatch.await(TEST_CASE_TIMEOUT, TimeUnit.SECONDS);

        assertEquals(ORDERS_NUMBER_IN_JSON, count[0]);
    }
}


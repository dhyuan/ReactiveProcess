package com.ech.kitchen.courier.service.impl;

import com.ech.kitchen.courier.service.ICookedOrderProvider;
import com.ech.kitchen.courier.service.ICourierService;
import com.ech.kitchen.mo.CookedOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class CourierService implements ICourierService {

    @Getter
    @Setter
    @Value("${courier.sleep.min:2000}")
    private int minInterval;

    @Getter
    @Setter
    @Value("${courier.sleep.max:6000}")
    private int maxInterval;

    @Getter
    @Setter
    @Value("${courier.worker.thread.pool.size:20}")
    private int courierWorkerThreadPoolSize = 20;

    @Autowired
    private ICookedOrderProvider orderProvider;

    private ExecutorService dispatcher = Executors.newSingleThreadExecutor();

    private ExecutorService courierWorkers;

    private volatile boolean isStarted = false;

    private final AtomicLong deliveryCounter = new AtomicLong(0);
    private final AtomicLong deliveryErrorCounter = new AtomicLong(0);

    public CourierService() {
    }

    @Override
    public void start() {
        if (isStarted) return;
        courierWorkers = Executors.newFixedThreadPool(courierWorkerThreadPoolSize);

        isStarted = true;
        dispatcher.execute(() -> {
            while (isStarted) {
                final int interval = randomInterval();
                try {
                    Thread.sleep(interval);

                    log.info("After {} milliseconds a courier requests a cooked order.", interval);
                    requestCookedOrder(orderProvider).ifPresent(order -> courierWorkers.submit(new CourierTask(order)));
                } catch (InterruptedException e) {
                    log.info(e.getMessage());
                } catch (Throwable t) {
                    final long errCount = deliveryErrorCounter.incrementAndGet();
                    log.error("Something wrong while try to request/delivery order. errCount=" + errCount, t);
                }
            }
        });
    }

    private int randomInterval() {
        final Random random = new Random();
        final int span = maxInterval - minInterval;
        return minInterval + random.nextInt(span);
    }

    @Override
    public void stop() {
        isStarted = false;
    }

    @Override
    public Optional<CookedOrder> requestCookedOrder(ICookedOrderProvider provider) {
        final Optional<CookedOrder> cookedOrderFromKitchen = provider.provideCookedOrder();
        log.info("Requested an order from kitchen: {}", cookedOrderFromKitchen);
        return cookedOrderFromKitchen;
    }

    @Override
    public long deliveryCount() {
        return deliveryCounter.longValue();
    }

    @Override
    public long deliveryErrorCount() {
        return deliveryErrorCounter.longValue();
    }

    class CourierTask implements Callable<CookedOrder> {
        CookedOrder cookedOrder;

        public CourierTask(CookedOrder order) {
            this.cookedOrder = order;
        }

        @Override
        public CookedOrder call() throws Exception {
            cookedOrder.setDeliveredTime(Instant.now());
            final long count = deliveryCounter.incrementAndGet();
            log.info("{} was delivered. Total delivered {} orders by now.", cookedOrder, count);
            return cookedOrder;
        }
    }
}

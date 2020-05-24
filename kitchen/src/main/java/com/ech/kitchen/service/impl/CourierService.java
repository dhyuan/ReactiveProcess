package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.CookedOrder;
import com.ech.kitchen.service.ICookedOrderProvider;
import com.ech.kitchen.service.ICourierService;
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

@Service
@Slf4j
public class CourierService implements ICourierService {

    private ExecutorService dispatcher = Executors.newSingleThreadExecutor();

    private ExecutorService courierWorkers = Executors.newCachedThreadPool();

    private volatile boolean isStarted = false;

    @Value("${courier.sleep.min:2000}")
    private int min_interval;

    @Value("${courier.sleep.max:6000}")
    private int max_interval;

    @Autowired
    private ICookedOrderProvider orderProvider;

    @Override
    public void start() {
        isStarted = true;
        dispatcher.execute(() -> {
            while (isStarted) {
                final int interval = randomInterval();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    log.info(e.getMessage());
                }
                log.info("After {} milliseconds a courier requests a cooked order.", interval);
                requestCookedOrder(orderProvider).ifPresent(order -> courierWorkers.submit(new CourierTask(order)));
            }
        });
    }

    private int randomInterval() {
        final Random random = new Random();
        final int span = max_interval - min_interval;
        return min_interval + random.nextInt(span);
    }

    @Override
    public void stop() {
        isStarted = false;
        courierWorkers.shutdown();
        dispatcher.shutdown();
    }

    @Override
    public Optional<CookedOrder> requestCookedOrder(ICookedOrderProvider provider) {
        final Optional<CookedOrder> cookedOrderFromKitchen = provider.provideCookedOrder();
        log.info("Requested an order from kitchen: {}", cookedOrderFromKitchen);
        return cookedOrderFromKitchen;
    }

    class CourierTask implements Callable<CookedOrder> {
        CookedOrder cookedOrder;

        public CourierTask(CookedOrder order) {
            this.cookedOrder = order;
        }

        @Override
        public CookedOrder call() throws Exception {
            cookedOrder.setDeliveredTime(Instant.now());
            log.info("{} was delivered.", cookedOrder);
            return cookedOrder;
        }
    }
}

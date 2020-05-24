package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.service.IExpiredOrderCheckingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class ExpiredOrderChecker implements IExpiredOrderCheckingService {

    @Value("${kitchen.order.checker.delay:2000}")
    private long checkerInitialDelay;

    @Value("${kitchen.order.checker.period:5000}")
    private long checkerPeriod;


    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private AtomicLong expiredOrderCounter = new AtomicLong(0);

    @Override
    public void workOn(Collection<Shelf> shelves) {
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                if (CollectionUtils.isEmpty(shelves)) {
                    return;
                }
                shelves.stream()
                        .filter(shelf -> shelf.getCookedOrderQueue().size() > 0)
                        .flatMap(shelf -> shelf.getCookedOrderQueue().stream())
                        .filter(cookedOrder -> cookedOrder != null
                                && cookedOrder.getOrderValue() != null
                                && cookedOrder.getOrderValue().isPresent()
                                && cookedOrder.getOrderValue().get() <= 0)
                        .forEach(cookedOrder -> {
                            cookedOrder.getShelf().remove(cookedOrder);
                            final long count = expiredOrderCounter.incrementAndGet();
                            log.info("{} is recycled. Total dropped ", cookedOrder, count);
                        });
            } catch (Throwable t) {
                log.error("There something wrong with cooked order recycle!", t);
                t.printStackTrace();
            }
        }, checkerInitialDelay, checkerPeriod, TimeUnit.MILLISECONDS);
    }

    @Override
    public long processedExpiredOrderNumb() {
        return expiredOrderCounter.longValue();
    }
}

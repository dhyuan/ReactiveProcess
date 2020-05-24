package com.ech.kitchen.service.impl;

import com.ech.kitchen.mo.Shelf;
import com.ech.kitchen.service.IPickupAreaRecycleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PickupAreaRecycleService implements IPickupAreaRecycleService {

    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

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
                            log.info("{} is recycled.", cookedOrder);
                        });
            } catch (Throwable t) {
                log.error("There something wrong with cooked order recycle!", t);
                t.printStackTrace();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }


}

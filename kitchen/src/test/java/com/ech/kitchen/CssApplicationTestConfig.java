package com.ech.kitchen;

//import com.ech.css.order.IOrderReceiver;
//import com.ech.css.order.impl.OrderFileReceiver;
import com.ech.kitchen.mo.Kitchen;
import com.ech.kitchen.service.ICookedOrderPickStrategy;
import com.ech.kitchen.service.ICourierService;
import com.ech.kitchen.service.IKitchenService;
import com.ech.kitchen.service.IPickupAreaRecycleService;
import com.ech.kitchen.service.IShelfSelectStrategy;
import com.ech.kitchen.service.impl.CookedOrderRandomPicker;
import com.ech.kitchen.service.impl.CourierService;
import com.ech.kitchen.service.impl.KitchenOrderObserver;
import com.ech.kitchen.service.impl.KitchenService;
import com.ech.kitchen.service.impl.PickupAreaRecycleService;
import com.ech.kitchen.service.impl.StrategyPutOrderOnShelf;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.impl.OrderFileScanner;
import com.ech.order.mo.Order;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@PropertySource("classpath:/kitchen-service-test.properties")
public class CssApplicationTestConfig {

    @Value("${order.file.name:orders.json:order.json}")
    private String orderFileName;

    @Value("#{${kitchen.shelf.capacity}}")
    private Map<String, Integer> shelfCapacities;

    @Value("${kitchen.shelf.capacity.default}")
    private int shelfDefaultCapacity;

    @Bean
    public IOrderScanner orderScanner() {
        return new OrderFileScanner(orderFileName);
    }

    @Bean
    public IOrderObserver<Order> orderObserver() {
        return new KitchenOrderObserver();
    }

    @Bean
    public IShelfSelectStrategy shelfChoicer() {
        return new StrategyPutOrderOnShelf();
    }

    @Bean
    public IPickupAreaRecycleService pickupAreaRecycleService() {
        return new PickupAreaRecycleService();
    }

    @Bean
    public ICookedOrderPickStrategy cookedOrderPickStrategy() {
        return new CookedOrderRandomPicker();
    }

    @Bean
    public IKitchenService kitchenService() {
        return new KitchenService();
    }

    @Bean
    public ICourierService courierService() {
        return new CourierService();
    }

    @Bean
    public Kitchen kitchen() {
        final Kitchen kitchen = new Kitchen();
        kitchen.setShelfCapacities(shelfCapacities);
        kitchen.setShelfDefaultCapacity(shelfDefaultCapacity);
        kitchen.buildPickupArea();
        return kitchen;
    }
}

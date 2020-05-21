package com.ech.kitchen;

//import com.ech.css.order.IOrderReceiver;
//import com.ech.css.order.impl.OrderFileReceiver;
import com.ech.kitchen.entity.Kitchen;
import com.ech.kitchen.service.IKitchenService;
import com.ech.kitchen.service.impl.KitchenOrderObserver;
import com.ech.kitchen.service.impl.KitchenService;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.impl.OrderFileScanner;
import com.ech.order.mo.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/kitchen_service.properties")
public class CssApplicationTestConfig {

    @Value("${order.file.name:orders.json:order.json}")
    private String orderFileName;

    @Bean
    public IOrderScanner orderScanner() {
        return new OrderFileScanner(orderFileName);
    }

    @Bean
    public IOrderObserver<Order> orderObserver() {
        return new KitchenOrderObserver();
    }

    @Bean
    public IKitchenService kitchenService() {
        return new KitchenService();
    }

    @Bean
    public Kitchen kitchen() {
        return new Kitchen();
    }
}

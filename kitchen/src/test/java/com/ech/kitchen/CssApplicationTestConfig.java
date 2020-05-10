package com.ech.kitchen;

//import com.ech.css.order.IOrderReceiver;
//import com.ech.css.order.impl.OrderFileReceiver;
import com.ech.kitchen.impl.KitchenOrderReceiver;
import com.ech.kitchen.impl.KitchenSystem;
import com.ech.order.IOrderObserver;
import com.ech.order.IOrderScanner;
import com.ech.order.Order;
import com.ech.order.impl.OrderFileScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CssApplicationTestConfig {

    @Value("${order.file.name:orders.json:order.json}")
    private String orderFileName;

    @Bean
    public IOrderScanner orderScannerA() {
        return new OrderFileScanner(orderFileName);
    }
//
//    @Bean
//    public IOrderObserver<Order> kitchenOrderReceiverA() {
//        return new KitchenOrderReceiver();
//    }
//
//    @Bean
//    public IKitchenSystem kitchenSystemA() {
//        return new KitchenSystem();
//    }
}

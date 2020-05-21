open module com.ech.kitchen {
    requires transitive com.ech.order;
    requires spring.boot.autoconfigure;
    requires org.apache.logging.log4j;
    requires spring.boot;
    requires org.slf4j;
    requires spring.context;
    requires org.reactivestreams;
    requires spring.beans;
    requires lombok;

    exports com.ech.kitchen.service;
    exports com.ech.kitchen.service.impl;

    uses com.ech.order.IOrderObserver;
}
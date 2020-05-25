open module com.ech.kitchen {
    requires transitive com.ech.order;
    requires org.apache.logging.log4j;
    requires org.apache.commons.collections4;
    requires org.slf4j;
    requires org.reactivestreams;
    requires lombok;
    requires spring.beans;
    requires spring.boot;
    requires spring.context;
    requires spring.boot.autoconfigure;
    requires java.annotation;
    requires org.mapstruct.processor;
    requires spring.core;

    exports com.ech.kitchen.mo;
    exports com.ech.kitchen.service;
    exports com.ech.kitchen.service.impl;

    uses com.ech.order.IOrderObserver;
}
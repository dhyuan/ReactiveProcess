module com.ech.order {
    requires com.fasterxml.jackson.annotation;
    requires org.reactivestreams;
    requires spring.beans;
    requires spring.context;
    requires com.fasterxml.jackson.core;
    requires org.apache.logging.log4j;
    requires reactor.core;
    requires com.fasterxml.jackson.databind;

    opens com.ech.order;
    opens com.ech.order.impl;

    exports com.ech.order;
    exports com.ech.order.impl;

    provides com.ech.order.IOrderScanner
            with com.ech.order.impl.OrderFileScanner;
}
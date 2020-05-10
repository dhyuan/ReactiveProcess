module com.ech.kitchen {
    requires com.ech.order;
    requires spring.boot.autoconfigure;
    requires org.apache.logging.log4j;
    requires spring.boot;
    requires spring.context;
    requires org.reactivestreams;
    requires spring.beans;

    exports com.ech.kitchen;
    exports com.ech.kitchen.impl;

    uses com.ech.order.IOrderScanner;

    opens com.ech.kitchen;
}